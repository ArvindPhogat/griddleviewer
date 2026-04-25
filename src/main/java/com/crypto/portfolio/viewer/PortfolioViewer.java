package com.crypto.portfolio.viewer;

import com.crypto.portfolio.calculation.OptionsPriceCalculator;
import com.crypto.portfolio.exception.PortfolioSnapshotMissingException;
import com.crypto.portfolio.model.*;
import com.crypto.portfolio.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import static com.crypto.portfolio.cache.SecurityDerivativesCache.derivativeCache;
import static com.crypto.portfolio.calculation.CommonCalculations.calPositionValue;
import static com.crypto.portfolio.constants.UtilityConstant.COMMA;
import static com.crypto.portfolio.domainValue.PositionType.fromPositionTypeString;
import static com.crypto.portfolio.domainValue.SecurityType.STOCK;
import static com.crypto.portfolio.domainValue.SecurityType.fromSecurityTypeString;

@Component
public class PortfolioViewer {

    @Autowired
    MarketService marketService;
    public List<Position> positions = new ArrayList<>();
    public  Portfolio portfolio;

    @Autowired
    JmsTemplate jmsTemplate;

    OptionsPriceCalculator optionsPriceCalculator = new OptionsPriceCalculator();
    Set<String> underlyingInPortfolio = new HashSet<>();

    @PostConstruct
    public void initPortfolio() {
        try {
            positionSnapshot();
        } catch (Exception e) {
            // Log ya ignore kar sakte hain
        }
    }
    public void positionSnapshot() throws PortfolioSnapshotMissingException {
        String snapshotCSVPath = "/app/resources/position/position.csv";
        try(Stream<String> lines = Files.lines(Paths.get(snapshotCSVPath))){
            lines.skip(1).forEach(line -> {
                String[] lineValues = line.split(COMMA);
                Position position = Position.builder().symbol(lineValues[0]).size(Integer.parseInt(lineValues[1]))
                        .positionType(fromPositionTypeString(lineValues[2])).underlying(lineValues[0].split("-")[0]).securityType(fromSecurityTypeString(lineValues[3])).build();
                positions.add(position);
                underlyingInPortfolio.add(position.getUnderlying());
            });
            portfolioSnapshot(positions);
        }catch(Exception ex){
            throw new PortfolioSnapshotMissingException("The portfolio snapshot CSV is missing");
        }
    }

    public void portfolioSnapshot(List<Position> positions){
        Map<String, Stock> stockMap = marketService.getStocksForCodes(underlyingInPortfolio).stream().collect(Collectors.toMap(Stock::getCode, stock-> stock));
        double nav =0.0;
        Map<String,PortfolioElement> stockElementMap = new HashMap<>();
        Map<String,List<PortfolioElement>> optionsElementMap = new HashMap<>();
        for (Position pos : positions){
            Stock stockInitVal = stockMap.get(pos.getUnderlying());
            if(STOCK.equals(pos.getSecurityType())){
                double price = stockInitVal.getDayStartPrice();
                double value = calPositionValue(pos.getPositionType(), price,pos.getSize());
                PortfolioElement positionEle = PortfolioElement.builder().quantity(pos.getSize()).positionType(pos.getPositionType())
                        .symbol(pos.getSymbol()).value(value).price(price).underlying(pos.getUnderlying()).securityType(pos.getSecurityType()).build();
                stockElementMap.put(pos.getUnderlying(), positionEle);
                nav = nav + value;
            }else{
                SecurityDefinition secDef = derivativeCache.get(pos.getSymbol());
                List<PortfolioElement> peOptionsList = optionsElementMap.getOrDefault(pos.getUnderlying(), new ArrayList<>());
                double price = optionsPriceCalculator.calculateOptionPrice(stockInitVal.getDayStartPrice(),secDef.getStrikePrice(),secDef.getMaturity(),secDef.getVolatility(),pos.getSecurityType());
                double value = calPositionValue(pos.getPositionType(), price,pos.getSize());
                PortfolioElement portfolioElement = PortfolioElement.builder().quantity(pos.getSize()).positionType(pos.getPositionType())
                        .symbol(pos.getSymbol()).value(value).price(price).underlying(pos.getUnderlying()).securityType(pos.getSecurityType()).build();
                peOptionsList.add(portfolioElement);
                optionsElementMap.put(pos.getUnderlying(),peOptionsList);
                nav = nav + value;
            }
        }
        portfolio = Portfolio.builder().stockElements(stockElementMap).optionsElements(optionsElementMap).nav(nav).build();
    }

    public void updateAndPublishPortfolio(String underlying , Double price){
        if(underlyingInPortfolio.contains(underlying)) {
            System.out.println("Change in stock price for    " + underlying + "  " + price);
            Map<String, PortfolioElement> stockElement = portfolio.getStockElements();
            if (stockElement.containsKey(underlying)) {
                PortfolioElement portfolioElement = stockElement.get(underlying);
                portfolioElement.setPrice(price);
                portfolioElement.setValue(calPositionValue(portfolioElement.getPositionType(), price, portfolioElement.getQuantity()));
                stockElement.put(underlying, portfolioElement);
                portfolio.setStockElements(stockElement);
            }
            Map<String, List<PortfolioElement>> optionsElement = portfolio.getOptionsElements();
            if (optionsElement.containsKey(underlying)) {
                List<PortfolioElement> portfolioElements = optionsElement.get(underlying);
                for (PortfolioElement pe : portfolioElements) {
                    SecurityDefinition secDef = derivativeCache.get(pe.getSymbol());
                    double optionPrice = optionsPriceCalculator.calculateOptionPrice(price, secDef.getStrikePrice(), secDef.getMaturity(), secDef.getVolatility(), pe.getSecurityType());
                    pe.setPrice(optionPrice);
                    pe.setValue(calPositionValue(pe.getPositionType(), optionPrice, pe.getQuantity()));
                }
                optionsElement.put(underlying, portfolioElements);
                portfolio.setOptionsElements(optionsElement);
            }
            portfolio.setNav(calculateUpdatedNav());
            if (portfolio != null) {
                jmsTemplate.convertAndSend("portfolio", portfolio);
            }
        }
    }

    private double calculateUpdatedNav() {
        double nav = 0.0;
        nav = nav + portfolio.getStockElements().values().stream().map(PortfolioElement::getValue).mapToDouble(Double::doubleValue).sum();
        nav = nav + portfolio.getOptionsElements().values().stream().flatMap(Collection::stream).map(PortfolioElement::getValue).mapToDouble(Double::doubleValue).sum();
        return nav;
    }

    // Naya method: Portfolio ka output String format me (browser ke liye)
    public String getPortfolioOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("TIME                ").append(java.time.ZonedDateTime.now()).append("\n\n");
        sb.append("    Positions           Symbol                      Price                      Quantity                    Value               \n\n");
        if (portfolio != null && portfolio.getStockElements() != null) {
            for (PortfolioElement pe : portfolio.getStockElements().values()) {
                sb.append(String.format("%25s%30s%30s%30s\n",
                        pe.getSymbol(),
                        pe.getPrice(),
                        pe.getQuantity(),
                        pe.getValue()
                ));
            }
        }
        if (portfolio != null && portfolio.getOptionsElements() != null) {
            for (java.util.List<PortfolioElement> list : portfolio.getOptionsElements().values()) {
                for (PortfolioElement pe : list) {
                    sb.append(String.format("%25s%30s%30s%30s\n",
                            pe.getSymbol(),
                            pe.getPrice(),
                            pe.getQuantity(),
                            pe.getValue()
                    ));
                }
            }
        }
        sb.append("\n    Net Asset Value     ").append(portfolio != null ? portfolio.getNav() : "NaN").append("\n");
        sb.append("_________________________________________________________________________________________________________________________________________________\n");
        return sb.toString();
    }

}

package com.crypto.portfolio.viewer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class PortfolioController {

    @Autowired
    private PortfolioViewer portfolioViewer;

    @GetMapping("/portfolio")
    public String getPortfolioOutput() {
        // PortfolioViewer ka method call karo jo output String return kare
        return portfolioViewer.getPortfolioOutput();
    }
}

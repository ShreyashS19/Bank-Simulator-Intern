package com.bank.simulator.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class RestApplication extends Application {
    // Jersey will automatically discover resources in the package
}

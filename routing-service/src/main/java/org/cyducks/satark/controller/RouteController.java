package org.cyducks.satark.controller;

import lombok.RequiredArgsConstructor;
import org.cyducks.satark.model.RouteRequest;
import org.cyducks.satark.model.RouteResponse;
import org.cyducks.satark.service.RouteFinderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/routing")
@RequiredArgsConstructor
public class RouteController {
    private final RouteFinderService routeFinderService;

    @PostMapping("/safe-route")
    public RouteResponse findSafeRoute(@RequestBody RouteRequest routeRequest) {
        return routeFinderService.findRoute(routeRequest);
    }
}

package com.hearth.care.controller;

import com.hearth.care.graph.GraphDtos.GraphView;
import com.hearth.care.graph.GraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/caregraph")
public class GraphController {

    private final GraphService graph;

    public GraphController(GraphService graph) {
        this.graph = graph;
    }

    /** The caller's family map: its people, the next shared visits and tasks, and how they connect, read from Neo4j. */
    @GetMapping
    public GraphView graph() {
        return graph.graph();
    }
}

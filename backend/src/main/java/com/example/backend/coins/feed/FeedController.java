package com.example.backend.coins.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
public class FeedController {

    private final FeedComposerService composerService;

    // Returns the composited feed: accepted trending first, then fallback fill.
    @GetMapping("/feed")
    public Flux<ComposedCoinView> feed() {
        return composerService.compose();
    }
}

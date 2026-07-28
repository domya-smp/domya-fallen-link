package ru.nyansus.mc.fallenlink.api;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import ru.nyansus.mc.fallenlink.model.PlayerLinkRequest;
import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;

public interface DomyaGateway extends AutoCloseable {

    CompletableFuture<ApiResponse> syncPlayers(Collection<PlayerSnapshot> players);

    CompletableFuture<LinkResult> linkPlayer(PlayerLinkRequest request);

    @Override
    void close();
}

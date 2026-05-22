package id.ac.ui.cs.advprog.auctionwallet.bidding.grpc;

import id.ac.ui.cs.advprog.auctionwallet.bidding.enums.AuctionStatus;
import id.ac.ui.cs.advprog.auctionwallet.bidding.exception.AuctionNotFoundException;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Auction;
import id.ac.ui.cs.advprog.auctionwallet.bidding.service.AuctionService;
import id.ac.ui.cs.advprog.auctionwallet.grpc.AuctionGrpcServiceGrpc;
import id.ac.ui.cs.advprog.auctionwallet.grpc.CancelUserBidsRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.CancelUserBidsResponse;
import id.ac.ui.cs.advprog.auctionwallet.grpc.CloseAuctionRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.CloseAuctionResponse;
import id.ac.ui.cs.advprog.auctionwallet.grpc.CreateAuctionRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.CreateAuctionResponse;
import id.ac.ui.cs.advprog.auctionwallet.grpc.GetAuctionStatusRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.GetAuctionStatusResponse;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({
    "PMD.UnitTestShouldIncludeAssert",
    "PMD.AvoidDuplicateLiterals",
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.UnitTestAssertionsShouldIncludeMessage",
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods",
    "PMD.LawOfDemeter"
})
class AuctionGrpcServiceTest {

    private static final String USER_ID    = "42";
    private static final Long   AUCTION_ID = 1L;
    private static final String START_TIME = "2030-01-01T10:00:00";
    private static final String END_TIME   = "2030-01-02T10:00:00";

    @Mock
    private AuctionService auctionService;

    private Server server;
    private ManagedChannel channel;
    private AuctionGrpcServiceGrpc.AuctionGrpcServiceBlockingStub stub;

    @BeforeEach
    void setUp() throws IOException {
        AuctionGrpcServiceImpl serviceImpl = new AuctionGrpcServiceImpl(auctionService);
        String serverName = InProcessServerBuilder.generateName();

        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(serviceImpl)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        stub = AuctionGrpcServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    private Auction buildAuction(AuctionStatus status, Long bidderId) {
        Auction a = new Auction();
        a.setId(AUCTION_ID);
        a.setStatus(status);
        a.setCurrentHighestBid(new BigDecimal("10000.00"));
        a.setCurrentHighestBidderId(bidderId);
        a.setEndTime(LocalDateTime.parse(END_TIME));
        return a;
    }

    @Test
    void testCreateAuctionSuccess() {
        when(auctionService.createAuction(any(), any(), any(), any(), any()))
                .thenReturn(buildAuction(AuctionStatus.ACTIVE, null));

        CreateAuctionResponse response = stub.createAuction(
                CreateAuctionRequest.newBuilder()
                        .setItemId(99L)
                        .setStartingPrice("5000.00")
                        .setMinimumIncrement("500.00")
                        .setStartTime(START_TIME)
                        .setEndTime(END_TIME)
                        .build());

        assertTrue(response.getSuccess());
        assertEquals(AUCTION_ID, response.getAuctionId());
    }

    @Test
    void testCreateAuctionServiceExceptionMapsToInternal() {
        when(auctionService.createAuction(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        CreateAuctionRequest request = CreateAuctionRequest.newBuilder()
                .setItemId(1L).setStartingPrice("100").setMinimumIncrement("10")
                .setStartTime(START_TIME).setEndTime(END_TIME).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.createAuction(request));
        assertEquals(io.grpc.Status.Code.INTERNAL, ex.getStatus().getCode());
    }

    @Test
    void testGetAuctionStatusSuccess() {
        when(auctionService.getAuctionById(AUCTION_ID))
                .thenReturn(buildAuction(AuctionStatus.ACTIVE, 42L));

        GetAuctionStatusResponse response = stub.getAuctionStatus(
                GetAuctionStatusRequest.newBuilder().setAuctionId(AUCTION_ID).build());

        assertEquals(AUCTION_ID, response.getAuctionId());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(42L, response.getCurrentHighestBidderId());
    }

    @Test
    void testGetAuctionStatusNotFoundMapsToNotFound() {
        when(auctionService.getAuctionById(anyLong()))
                .thenThrow(new AuctionNotFoundException(AUCTION_ID));

        GetAuctionStatusRequest request = GetAuctionStatusRequest.newBuilder()
                .setAuctionId(999L).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.getAuctionStatus(request));
        assertEquals(io.grpc.Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }

    @Test
    void testCancelUserBidsSuccess() {
        when(auctionService.cancelUserBids(Long.parseLong(USER_ID))).thenReturn(3);

        CancelUserBidsResponse response = stub.cancelUserBids(
                CancelUserBidsRequest.newBuilder().setUserId(USER_ID).build());

        assertTrue(response.getSuccess());
        assertEquals(3, response.getBidsCancelled());
    }

    @Test
    void testCancelUserBidsZeroBids() {
        when(auctionService.cancelUserBids(anyLong())).thenReturn(0);

        CancelUserBidsResponse response = stub.cancelUserBids(
                CancelUserBidsRequest.newBuilder().setUserId(USER_ID).build());

        assertTrue(response.getSuccess());
        assertEquals(0, response.getBidsCancelled());
    }

    @Test
    void testCancelUserBidsServiceExceptionMapsToInternal() {
        when(auctionService.cancelUserBids(anyLong()))
                .thenThrow(new RuntimeException("DB error"));

        CancelUserBidsRequest request = CancelUserBidsRequest.newBuilder()
                .setUserId(USER_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.cancelUserBids(request));
        assertEquals(io.grpc.Status.Code.INTERNAL, ex.getStatus().getCode());
    }

    @Test
    void testCloseAuctionWon() {
        when(auctionService.closeAuction(AUCTION_ID))
                .thenReturn(buildAuction(AuctionStatus.WON, 42L));

        CloseAuctionResponse response = stub.closeAuction(
                CloseAuctionRequest.newBuilder().setAuctionId(AUCTION_ID).build());

        assertTrue(response.getSuccess());
        assertEquals("WON", response.getStatus());
    }

    @Test
    void testCloseAuctionUnsold() {
        when(auctionService.closeAuction(AUCTION_ID))
                .thenReturn(buildAuction(AuctionStatus.UNSOLD, null));

        CloseAuctionResponse response = stub.closeAuction(
                CloseAuctionRequest.newBuilder().setAuctionId(AUCTION_ID).build());

        assertTrue(response.getSuccess());
        assertEquals("UNSOLD", response.getStatus());
    }

    @Test
    void testCloseAuctionNotFoundMapsToNotFound() {
        when(auctionService.closeAuction(anyLong()))
                .thenThrow(new AuctionNotFoundException(AUCTION_ID));

        CloseAuctionRequest request = CloseAuctionRequest.newBuilder()
                .setAuctionId(999L).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.closeAuction(request));
        assertEquals(io.grpc.Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }
}

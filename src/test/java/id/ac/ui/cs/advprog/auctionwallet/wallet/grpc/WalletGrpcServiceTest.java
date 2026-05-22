package id.ac.ui.cs.advprog.auctionwallet.wallet.grpc;

import id.ac.ui.cs.advprog.auctionwallet.grpc.CreateWalletRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.CreateWalletResponse;
import id.ac.ui.cs.advprog.auctionwallet.grpc.GetBalanceRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.GetBalanceResponse;
import id.ac.ui.cs.advprog.auctionwallet.grpc.GetHistoryRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.GetHistoryResponse;
import id.ac.ui.cs.advprog.auctionwallet.grpc.HoldFundsRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.HoldFundsResponse;
import id.ac.ui.cs.advprog.auctionwallet.grpc.ReleaseFundsRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.ReleaseFundsResponse;
import id.ac.ui.cs.advprog.auctionwallet.grpc.SettleFundsRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.SettleFundsResponse;
import id.ac.ui.cs.advprog.auctionwallet.grpc.TopUpRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.TopUpResponse;
import id.ac.ui.cs.advprog.auctionwallet.grpc.WalletGrpcServiceGrpc;
import id.ac.ui.cs.advprog.auctionwallet.grpc.WithdrawRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.WithdrawResponse;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.TransactionType;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.auctionwallet.wallet.service.WalletService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({
    "PMD.UnitTestShouldIncludeAssert",
    "PMD.AvoidDuplicateLiterals",
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.UnitTestAssertionsShouldIncludeMessage",
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods",
    "PMD.LawOfDemeter",
    "PMD.CouplingBetweenObjects"
})
class WalletGrpcServiceTest {

    private static final String USER_ID              = "user-123";
    private static final String AUC_ID               = "auc-001";
    private static final String WALLET_NOT_FOUND_MSG = "Wallet not found";

    @Mock
    private WalletService walletService;

    private Server server;
    private ManagedChannel channel;
    private WalletGrpcServiceGrpc.WalletGrpcServiceBlockingStub stub;

    @BeforeEach
    void setUp() throws IOException {
        WalletGrpcServiceImpl serviceImpl = new WalletGrpcServiceImpl(walletService);
        String serverName = InProcessServerBuilder.generateName();

        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(serviceImpl)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        stub = WalletGrpcServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void testCreateWalletReturnsSuccessAndUserId() {
        Wallet wallet = new Wallet(USER_ID);
        when(walletService.getWallet(USER_ID)).thenReturn(wallet);

        CreateWalletResponse response = stub.createWallet(
                CreateWalletRequest.newBuilder().setUserId(USER_ID).build());

        assertTrue(response.getSuccess());
        assertEquals(USER_ID, response.getUserId());
        verify(walletService).getWallet(USER_ID);
    }

    @Test
    void testCreateWalletServiceExceptionMapsToInternal() {
        when(walletService.getWallet(USER_ID)).thenThrow(new RuntimeException("DB error"));

        CreateWalletRequest request = CreateWalletRequest.newBuilder().setUserId(USER_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.createWallet(request));

        assertEquals(io.grpc.Status.Code.INTERNAL, ex.getStatus().getCode());
    }

    @Test
    void testGetWalletBalanceReturnsCorrectValues() {
        Wallet wallet = new Wallet(USER_ID);
        wallet.addBalance(new BigDecimal("75000.00"));
        wallet.holdBalance(new BigDecimal("25000.00"));
        when(walletService.getWallet(USER_ID)).thenReturn(wallet);

        GetBalanceResponse response = stub.getWalletBalance(
                GetBalanceRequest.newBuilder().setUserId(USER_ID).build());

        assertEquals(USER_ID, response.getUserId());
        assertEquals("50000.00", response.getAvailableBalance());
        assertEquals("25000.00", response.getHeldBalance());
    }

    @Test
    void testGetWalletBalanceNewUserHasZeroBalances() {
        Wallet wallet = new Wallet(USER_ID);
        when(walletService.getWallet(USER_ID)).thenReturn(wallet);

        GetBalanceResponse response = stub.getWalletBalance(
                GetBalanceRequest.newBuilder().setUserId(USER_ID).build());

        assertEquals("0", response.getAvailableBalance());
        assertEquals("0", response.getHeldBalance());
    }

    @Test
    void testGetWalletBalanceServiceExceptionMapsToInternal() {
        when(walletService.getWallet(USER_ID)).thenThrow(new RuntimeException("DB error"));

        GetBalanceRequest request = GetBalanceRequest.newBuilder().setUserId(USER_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.getWalletBalance(request));

        assertEquals(io.grpc.Status.Code.INTERNAL, ex.getStatus().getCode());
    }

    @Test
    void testTopUpSuccess() {
        Wallet wallet = new Wallet(USER_ID);
        wallet.addBalance(new BigDecimal("150000.00"));
        doNothing().when(walletService).topUp(eq(USER_ID), any(BigDecimal.class));
        when(walletService.getWallet(USER_ID)).thenReturn(wallet);

        TopUpResponse response = stub.topUp(
                TopUpRequest.newBuilder().setUserId(USER_ID).setAmount("50000.00").build());

        assertTrue(response.getSuccess());
        assertEquals("Top-up successful", response.getMessage());
    }

    @Test
    void testTopUpWalletNotFoundMapsToNotFound() {
        doThrow(new WalletNotFoundException(WALLET_NOT_FOUND_MSG))
                .when(walletService).topUp(any(), any());

        TopUpRequest request = TopUpRequest.newBuilder()
                .setUserId("unknown").setAmount("1000.00").build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.topUp(request));

        assertEquals(io.grpc.Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }

    @Test
    void testTopUpServiceExceptionMapsToInternal() {
        doThrow(new RuntimeException("DB error"))
                .when(walletService).topUp(any(), any());

        TopUpRequest request = TopUpRequest.newBuilder()
                .setUserId(USER_ID).setAmount("1000.00").build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.topUp(request));

        assertEquals(io.grpc.Status.Code.INTERNAL, ex.getStatus().getCode());
    }

    @Test
    void testWithdrawSuccess() {
        Wallet wallet = new Wallet(USER_ID);
        wallet.addBalance(new BigDecimal("60000.00"));
        doNothing().when(walletService).withdraw(eq(USER_ID), any(BigDecimal.class));
        when(walletService.getWallet(USER_ID)).thenReturn(wallet);

        WithdrawResponse response = stub.withdraw(
                WithdrawRequest.newBuilder().setUserId(USER_ID).setAmount("40000.00").build());

        assertTrue(response.getSuccess());
        assertEquals("Withdrawal successful", response.getMessage());
    }

    @Test
    void testWithdrawWalletNotFoundMapsToNotFound() {
        doThrow(new WalletNotFoundException(WALLET_NOT_FOUND_MSG))
                .when(walletService).withdraw(any(), any());

        WithdrawRequest request = WithdrawRequest.newBuilder()
                .setUserId("unknown").setAmount("100.00").build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.withdraw(request));

        assertEquals(io.grpc.Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }

    @Test
    void testWithdrawInsufficientBalanceMapsToFailedPrecondition() {
        doThrow(new InsufficientBalanceException("Insufficient balance"))
                .when(walletService).withdraw(any(), any());

        WithdrawRequest request = WithdrawRequest.newBuilder()
                .setUserId(USER_ID).setAmount("999999.00").build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.withdraw(request));

        assertEquals(io.grpc.Status.Code.FAILED_PRECONDITION, ex.getStatus().getCode());
    }

    @Test
    void testWithdrawServiceExceptionMapsToInternal() {
        doThrow(new RuntimeException("DB error"))
                .when(walletService).withdraw(any(), any());

        WithdrawRequest request = WithdrawRequest.newBuilder()
                .setUserId(USER_ID).setAmount("100.00").build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.withdraw(request));

        assertEquals(io.grpc.Status.Code.INTERNAL, ex.getStatus().getCode());
    }

    @Test
    void testGetTransactionHistoryReturnsRecords() {
        WalletTransaction tx = new WalletTransaction(
                USER_ID, TransactionType.TOP_UP, new BigDecimal("50000"),
                BigDecimal.ZERO, new BigDecimal("50000"), null);
        when(walletService.getHistory(USER_ID)).thenReturn(List.of(tx));

        GetHistoryResponse response = stub.getTransactionHistory(
                GetHistoryRequest.newBuilder().setUserId(USER_ID).build());

        assertEquals(USER_ID, response.getUserId());
        assertEquals(1, response.getRecordsCount());
        assertEquals("TOP_UP", response.getRecords(0).getTransactionType());
    }

    @Test
    void testGetTransactionHistoryEmptyList() {
        when(walletService.getHistory(USER_ID)).thenReturn(List.of());

        GetHistoryResponse response = stub.getTransactionHistory(
                GetHistoryRequest.newBuilder().setUserId(USER_ID).build());

        assertEquals(0, response.getRecordsCount());
    }

    @Test
    void testGetTransactionHistoryServiceExceptionMapsToInternal() {
        when(walletService.getHistory(USER_ID)).thenThrow(new RuntimeException("DB error"));

        GetHistoryRequest request = GetHistoryRequest.newBuilder().setUserId(USER_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.getTransactionHistory(request));

        assertEquals(io.grpc.Status.Code.INTERNAL, ex.getStatus().getCode());
    }

    @Test
    void testHoldFundsSuccessReturnsTrue() {
        doNothing().when(walletService).holdForBid(eq(USER_ID), any(BigDecimal.class), eq(AUC_ID));

        HoldFundsResponse response = stub.holdFunds(
                HoldFundsRequest.newBuilder()
                        .setUserId(USER_ID)
                        .setAmount("10000.00")
                        .setReferenceId(AUC_ID)
                        .build());

        assertTrue(response.getSuccess());
        verify(walletService).holdForBid(USER_ID, new BigDecimal("10000.00"), AUC_ID);
    }

    @Test
    void testHoldFundsWalletNotFoundMapsToNotFound() {
        doThrow(new WalletNotFoundException(WALLET_NOT_FOUND_MSG))
                .when(walletService).holdForBid(any(), any(), any());

        HoldFundsRequest request = HoldFundsRequest.newBuilder()
                .setUserId("unknown").setAmount("100.00").setReferenceId(AUC_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.holdFunds(request));

        assertEquals(io.grpc.Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }

    @Test
    void testHoldFundsInsufficientBalanceMapsToFailedPrecondition() {
        doThrow(new InsufficientBalanceException("Insufficient balance"))
                .when(walletService).holdForBid(any(), any(), any());

        HoldFundsRequest request = HoldFundsRequest.newBuilder()
                .setUserId(USER_ID).setAmount("999999.00").setReferenceId(AUC_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.holdFunds(request));

        assertEquals(io.grpc.Status.Code.FAILED_PRECONDITION, ex.getStatus().getCode());
    }

    @Test
    void testHoldFundsServiceExceptionMapsToInternal() {
        doThrow(new RuntimeException("DB error"))
                .when(walletService).holdForBid(any(), any(), any());

        HoldFundsRequest request = HoldFundsRequest.newBuilder()
                .setUserId(USER_ID).setAmount("100.00").setReferenceId(AUC_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.holdFunds(request));

        assertEquals(io.grpc.Status.Code.INTERNAL, ex.getStatus().getCode());
    }

    @Test
    void testReleaseFundsSuccessReturnsTrue() {
        doNothing().when(walletService).releaseFromBid(any(), any(), any());

        ReleaseFundsResponse response = stub.releaseFunds(
                ReleaseFundsRequest.newBuilder()
                        .setUserId(USER_ID).setAmount("10000.00").setReferenceId(AUC_ID).build());

        assertTrue(response.getSuccess());
    }

    @Test
    void testReleaseFundsWalletNotFoundMapsToNotFound() {
        doThrow(new WalletNotFoundException(WALLET_NOT_FOUND_MSG))
                .when(walletService).releaseFromBid(any(), any(), any());

        ReleaseFundsRequest request = ReleaseFundsRequest.newBuilder()
                .setUserId("unknown").setAmount("100.00").setReferenceId(AUC_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.releaseFunds(request));

        assertEquals(io.grpc.Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }

    @Test
    void testReleaseFundsInsufficientHeldMapsToFailedPrecondition() {
        doThrow(new InsufficientBalanceException("Insufficient held balance"))
                .when(walletService).releaseFromBid(any(), any(), any());

        ReleaseFundsRequest request = ReleaseFundsRequest.newBuilder()
                .setUserId(USER_ID).setAmount("999999.00").setReferenceId(AUC_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.releaseFunds(request));

        assertEquals(io.grpc.Status.Code.FAILED_PRECONDITION, ex.getStatus().getCode());
    }

    @Test
    void testReleaseFundsServiceExceptionMapsToInternal() {
        doThrow(new RuntimeException("DB error"))
                .when(walletService).releaseFromBid(any(), any(), any());

        ReleaseFundsRequest request = ReleaseFundsRequest.newBuilder()
                .setUserId(USER_ID).setAmount("100.00").setReferenceId(AUC_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.releaseFunds(request));

        assertEquals(io.grpc.Status.Code.INTERNAL, ex.getStatus().getCode());
    }

    @Test
    void testSettleFundsSuccessReturnsTrue() {
        doNothing().when(walletService).payFromHeld(any(), any(), any());

        SettleFundsResponse response = stub.settleFunds(
                SettleFundsRequest.newBuilder()
                        .setUserId(USER_ID).setAmount("10000.00").setReferenceId(AUC_ID).build());

        assertTrue(response.getSuccess());
    }

    @Test
    void testSettleFundsWalletNotFoundMapsToNotFound() {
        doThrow(new WalletNotFoundException(WALLET_NOT_FOUND_MSG))
                .when(walletService).payFromHeld(any(), any(), any());

        SettleFundsRequest request = SettleFundsRequest.newBuilder()
                .setUserId("unknown").setAmount("100.00").setReferenceId(AUC_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.settleFunds(request));

        assertEquals(io.grpc.Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }

    @Test
    void testSettleFundsInsufficientHeldMapsToFailedPrecondition() {
        doThrow(new InsufficientBalanceException("Insufficient held balance"))
                .when(walletService).payFromHeld(any(), any(), any());

        SettleFundsRequest request = SettleFundsRequest.newBuilder()
                .setUserId(USER_ID).setAmount("999999.00").setReferenceId(AUC_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.settleFunds(request));

        assertEquals(io.grpc.Status.Code.FAILED_PRECONDITION, ex.getStatus().getCode());
    }

    @Test
    void testSettleFundsServiceExceptionMapsToInternal() {
        doThrow(new RuntimeException("DB error"))
                .when(walletService).payFromHeld(any(), any(), any());

        SettleFundsRequest request = SettleFundsRequest.newBuilder()
                .setUserId(USER_ID).setAmount("100.00").setReferenceId(AUC_ID).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.settleFunds(request));

        assertEquals(io.grpc.Status.Code.INTERNAL, ex.getStatus().getCode());
    }

    @Test
    void testHoldFundsResponseMessageIsSet() {
        doNothing().when(walletService).holdForBid(any(), any(), any());

        HoldFundsResponse response = stub.holdFunds(
                HoldFundsRequest.newBuilder()
                        .setUserId(USER_ID).setAmount("500.00").setReferenceId(AUC_ID).build());

        assertFalse(response.getMessage().isEmpty());
    }
}
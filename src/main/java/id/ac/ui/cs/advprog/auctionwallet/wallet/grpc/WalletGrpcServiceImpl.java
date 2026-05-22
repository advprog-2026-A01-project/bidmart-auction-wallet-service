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
import id.ac.ui.cs.advprog.auctionwallet.grpc.TransactionRecord;
import id.ac.ui.cs.advprog.auctionwallet.grpc.WalletGrpcServiceGrpc;
import id.ac.ui.cs.advprog.auctionwallet.grpc.WithdrawRequest;
import id.ac.ui.cs.advprog.auctionwallet.grpc.WithdrawResponse;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.Wallet;
import id.ac.ui.cs.advprog.auctionwallet.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.auctionwallet.wallet.service.WalletService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import java.math.BigDecimal;
import java.util.List;

@GrpcService
@SuppressWarnings({"PMD.LawOfDemeter", "PMD.AvoidDuplicateLiterals", "PMD.AvoidCatchingGenericException", "PMD.CouplingBetweenObjects"})
public class WalletGrpcServiceImpl extends WalletGrpcServiceGrpc.WalletGrpcServiceImplBase {

    private static final String FUNDS_HELD_MSG      = "Funds held successfully";
    private static final String FUNDS_RELEASED_MSG  = "Funds released successfully";
    private static final String FUNDS_SETTLED_MSG   = "Payment settled successfully";
    private static final String TOP_UP_MSG          = "Top-up successful";
    private static final String WITHDRAW_MSG        = "Withdrawal successful";

    private final WalletService walletService;

    public WalletGrpcServiceImpl(WalletService walletService) {
        this.walletService = walletService;
    }

    @Override
    public void createWallet(CreateWalletRequest request,
                             StreamObserver<CreateWalletResponse> responseObserver) {
        try {
            walletService.getWallet(request.getUserId());
            responseObserver.onNext(CreateWalletResponse.newBuilder()
                    .setSuccess(true)
                    .setUserId(request.getUserId())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getWalletBalance(GetBalanceRequest request,
                                 StreamObserver<GetBalanceResponse> responseObserver) {
        try {
            Wallet wallet = walletService.getWallet(request.getUserId());
            responseObserver.onNext(GetBalanceResponse.newBuilder()
                    .setUserId(request.getUserId())
                    .setAvailableBalance(wallet.getAvailableBalance().toPlainString())
                    .setHeldBalance(wallet.getHeldBalance().toPlainString())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @Override
    public void topUp(TopUpRequest request,
                      StreamObserver<TopUpResponse> responseObserver) {
        try {
            walletService.topUp(request.getUserId(), new BigDecimal(request.getAmount()));
            Wallet wallet = walletService.getWallet(request.getUserId());
            responseObserver.onNext(TopUpResponse.newBuilder()
                    .setSuccess(true)
                    .setAvailableBalance(wallet.getAvailableBalance().toPlainString())
                    .setMessage(TOP_UP_MSG)
                    .build());
            responseObserver.onCompleted();
        } catch (WalletNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @Override
    public void withdraw(WithdrawRequest request,
                         StreamObserver<WithdrawResponse> responseObserver) {
        try {
            walletService.withdraw(request.getUserId(), new BigDecimal(request.getAmount()));
            Wallet wallet = walletService.getWallet(request.getUserId());
            responseObserver.onNext(WithdrawResponse.newBuilder()
                    .setSuccess(true)
                    .setAvailableBalance(wallet.getAvailableBalance().toPlainString())
                    .setMessage(WITHDRAW_MSG)
                    .build());
            responseObserver.onCompleted();
        } catch (WalletNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (InsufficientBalanceException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getTransactionHistory(GetHistoryRequest request,
                                      StreamObserver<GetHistoryResponse> responseObserver) {
        try {
            List<WalletTransaction> txList = walletService.getHistory(request.getUserId());
            GetHistoryResponse.Builder builder = GetHistoryResponse.newBuilder()
                    .setUserId(request.getUserId());
            for (WalletTransaction tx : txList) {
                builder.addRecords(TransactionRecord.newBuilder()
                        .setTransactionType(tx.getType().name())
                        .setAmount(tx.getAmount().toPlainString())
                        .setBalanceBefore(tx.getBalanceBefore().toPlainString())
                        .setBalanceAfter(tx.getBalanceAfter().toPlainString())
                        .setReferenceId(tx.getReferenceId() != null ? tx.getReferenceId() : "")
                        .setCreatedAt(tx.getCreatedAt().toString())
                        .build());
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @Override
    public void holdFunds(HoldFundsRequest request,
                          StreamObserver<HoldFundsResponse> responseObserver) {
        try {
            walletService.holdForBid(
                    request.getUserId(),
                    new BigDecimal(request.getAmount()),
                    request.getReferenceId());
            responseObserver.onNext(HoldFundsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage(FUNDS_HELD_MSG)
                    .build());
            responseObserver.onCompleted();
        } catch (WalletNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (InsufficientBalanceException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @Override
    public void releaseFunds(ReleaseFundsRequest request,
                             StreamObserver<ReleaseFundsResponse> responseObserver) {
        try {
            walletService.releaseFromBid(
                    request.getUserId(),
                    new BigDecimal(request.getAmount()),
                    request.getReferenceId());
            responseObserver.onNext(ReleaseFundsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage(FUNDS_RELEASED_MSG)
                    .build());
            responseObserver.onCompleted();
        } catch (WalletNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (InsufficientBalanceException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @Override
    public void settleFunds(SettleFundsRequest request,
                            StreamObserver<SettleFundsResponse> responseObserver) {
        try {
            walletService.payFromHeld(
                    request.getUserId(),
                    new BigDecimal(request.getAmount()),
                    request.getReferenceId());
            responseObserver.onNext(SettleFundsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage(FUNDS_SETTLED_MSG)
                    .build());
            responseObserver.onCompleted();
        } catch (WalletNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (InsufficientBalanceException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }
}

package id.ac.ui.cs.advprog.auctionwallet.bidding.grpc;

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
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@GrpcService
@SuppressWarnings({"PMD.LawOfDemeter", "PMD.AvoidDuplicateLiterals", "PMD.AvoidCatchingGenericException"})
public class AuctionGrpcServiceImpl extends AuctionGrpcServiceGrpc.AuctionGrpcServiceImplBase {

    private static final String AUCTION_CREATED_MSG  = "Auction created successfully";
    private static final String AUCTION_CLOSED_MSG   = "Auction closed successfully";
    private static final String BIDS_CANCELLED_MSG   = "User bids cancelled and funds released";

    private final AuctionService auctionService;

    public AuctionGrpcServiceImpl(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public void createAuction(CreateAuctionRequest request,
                              StreamObserver<CreateAuctionResponse> responseObserver) {
        try {
            Auction auction = auctionService.createAuction(
                    request.getItemId(),
                    new BigDecimal(request.getStartingPrice()),
                    new BigDecimal(request.getMinimumIncrement()),
                    LocalDateTime.parse(request.getStartTime()),
                    LocalDateTime.parse(request.getEndTime())
            );

            responseObserver.onNext(CreateAuctionResponse.newBuilder()
                    .setSuccess(true)
                    .setAuctionId(auction.getId())
                    .setMessage(AUCTION_CREATED_MSG)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getAuctionStatus(GetAuctionStatusRequest request,
                                 StreamObserver<GetAuctionStatusResponse> responseObserver) {
        try {
            Auction auction = auctionService.getAuctionById(request.getAuctionId());

            long highestBidderId = auction.getCurrentHighestBidderId() != null
                    ? auction.getCurrentHighestBidderId() : 0L;

            responseObserver.onNext(GetAuctionStatusResponse.newBuilder()
                    .setAuctionId(auction.getId())
                    .setStatus(auction.getStatus().name())
                    .setCurrentHighestBid(auction.getCurrentHighestBid().toPlainString())
                    .setCurrentHighestBidderId(highestBidderId)
                    .setEndTime(auction.getEndTime().toString())
                    .build());
            responseObserver.onCompleted();
        } catch (AuctionNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @Override
    public void cancelUserBids(CancelUserBidsRequest request,
                               StreamObserver<CancelUserBidsResponse> responseObserver) {
        try {
            int cancelled = auctionService.cancelUserBids(Long.parseLong(request.getUserId()));

            responseObserver.onNext(CancelUserBidsResponse.newBuilder()
                    .setSuccess(true)
                    .setBidsCancelled(cancelled)
                    .setMessage(BIDS_CANCELLED_MSG)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @Override
    public void closeAuction(CloseAuctionRequest request,
                             StreamObserver<CloseAuctionResponse> responseObserver) {
        try {
            Auction auction = auctionService.closeAuction(request.getAuctionId());

            responseObserver.onNext(CloseAuctionResponse.newBuilder()
                    .setSuccess(true)
                    .setStatus(auction.getStatus().name())
                    .setMessage(AUCTION_CLOSED_MSG)
                    .build());
            responseObserver.onCompleted();
        } catch (AuctionNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }
}

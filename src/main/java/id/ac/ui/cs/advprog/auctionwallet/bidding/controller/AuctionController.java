package id.ac.ui.cs.advprog.auctionwallet.bidding.controller;

import id.ac.ui.cs.advprog.auctionwallet.bidding.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Bid;
import id.ac.ui.cs.advprog.auctionwallet.bidding.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<?> placeBid(
            @PathVariable Long auctionId,
            @RequestBody BidRequestDTO bidRequest) {

        try {
            Bid acceptedBid = auctionService.placeBid(
                    auctionId,
                    bidRequest.getUserId(),
                    bidRequest.getAmount()
            );
            return ResponseEntity.ok(acceptedBid);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
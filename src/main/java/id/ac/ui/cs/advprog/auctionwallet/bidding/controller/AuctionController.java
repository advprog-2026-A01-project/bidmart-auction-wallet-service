package id.ac.ui.cs.advprog.auctionwallet.bidding.controller;

import id.ac.ui.cs.advprog.auctionwallet.bidding.dto.BidRequestDTO;
import id.ac.ui.cs.advprog.auctionwallet.bidding.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.auctionwallet.bidding.model.Auction;
import id.ac.ui.cs.advprog.auctionwallet.bidding.service.AuctionService;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @GetMapping
    public ResponseEntity<List<Auction>> getAllAuctions() {
        return ResponseEntity.ok(auctionService.getAllAuctions());
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<Auction> getAuctionById(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getAuctionById(auctionId));
    }

    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<BidResponseDTO> placeBid(
            @PathVariable Long auctionId,
            @Valid @RequestBody BidRequestDTO request
    ) {

        BidResponseDTO response =
                auctionService.placeBid(
                        auctionId,
                        request.getUserId(),
                        request.getAmount()
                );

        return ResponseEntity.ok(response);
    }
}
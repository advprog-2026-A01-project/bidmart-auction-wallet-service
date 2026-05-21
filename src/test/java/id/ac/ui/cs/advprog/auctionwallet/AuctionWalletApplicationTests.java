package id.ac.ui.cs.advprog.auctionwallet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "grpc.server.port=0"
})
@SuppressWarnings({"PMD.UnitTestShouldIncludeAssert", "PMD.AvoidDuplicateLiterals", "PMD.UnitTestContainsTooManyAsserts", "PMD.UnitTestAssertionsShouldIncludeMessage"})
class AuctionWalletApplicationTests {

    @Test
    void contextLoads() {
    }

}

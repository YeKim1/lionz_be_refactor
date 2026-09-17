package haja.Project.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class RefreshTokenTest {
    @Test
    void rotatesBothTokensInPlaceAndPreservesOwner() {
        RefreshToken token = RefreshToken.builder().key("7").value("old-refresh").accessToken("old-access").build();
        assertThat(token).extracting("key", "value", "accessToken").containsExactly("7", "old-refresh", "old-access");
        assertThat(token.updateValue("new-refresh", "new-access")).isSameAs(token);
        assertThat(token).extracting("key", "value", "accessToken").containsExactly("7", "new-refresh", "new-access");
    }
}

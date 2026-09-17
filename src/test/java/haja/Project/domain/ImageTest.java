package haja.Project.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ImageTest {
    @Test
    void storesPublicUrlFilenameAndDiskPathSeparately() {
        Image image = new Image("/img/a.png", "a.png", "/tmp/a.png");
        assertThat(image).extracting("img_link", "img_name", "img_path").containsExactly("/img/a.png", "a.png", "/tmp/a.png");
        image.setImg_link("/img/b.png"); image.setImg_name("b.png"); image.setImg_path("/tmp/b.png");
        assertThat(image).extracting("img_link", "img_name", "img_path").containsExactly("/img/b.png", "b.png", "/tmp/b.png");
    }
}

import org.junit.Test;
import photodb.ConnectionFactory;
import photodb.Picture;
import photodb.PictureDAO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.Connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DBTest {

    @Test
    public void testConnection() {
        assertNotNull(ConnectionFactory.getConnection());
    }

    @Test
    public void testDbPopulate() throws Exception {
        Connection c = ConnectionFactory.getConnection();
        BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        Picture p = new Picture(image, 0, "rgm", "jpg");
        PictureDAO dao = new PictureDAO(p);

        int oldCount = dao.count();

        dao.insert();

        assertEquals(oldCount + 1, dao.count());

    }

}

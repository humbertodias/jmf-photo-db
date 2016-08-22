
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import photodb.ConnectionFactory;
import photodb.Picture;
import photodb.PictureDAO;
import org.junit.Assert;
import org.junit.Test;

public class DBTest {

    @Test
    public void testConnection() throws ClassNotFoundException {
        Assert.assertNotNull(ConnectionFactory.getConnection());
    }

    @Test
    public void testDbPopulate() throws AWTException, ClassNotFoundException, Exception {
        Connection c = ConnectionFactory.getConnection();
        BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        Picture p = new Picture(image, 0, "rgm", "jpg");
        PictureDAO dao = new PictureDAO(p);

        int oldCount = dao.count();

        dao.insert();

        Assert.assertEquals(oldCount + 1, dao.count());

    }

}

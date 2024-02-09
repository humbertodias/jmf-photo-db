package photodb;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.imageio.ImageIO;

import static photodb.ConnectionFactory.*;

public class PictureDAO {
    private Picture picture;

    public PictureDAO(Picture picture) {
        this.picture = picture;
    }

    public int count() throws Exception {
        try (Connection conn = ConnectionFactory.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(TABLE_PHOTO_SQL_COUNT)) {

                pstmt.setInt(1, picture.getInstituicao());
                pstmt.setString(2, picture.getRGM());
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                // count
                return rs.getInt(1);
            }

        }
    }

    public BufferedImage getFoto() throws Exception {
        try (Connection conn = ConnectionFactory.getConnection()) {

            try (PreparedStatement pstmt = conn.prepareStatement(TABLE_PHOTO_SQL_SELECT)) {
                InputStream is = null;
                BufferedImage bi = null;
                pstmt.setInt(1, picture.getInstituicao());
                pstmt.setString(2, picture.getRGM());
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                is = rs.getBinaryStream(1);
                return ImageIO.read(is);
            }
        }
    }

    public void insert() throws Exception {
        try (InputStream is = picture.getBinaryStream()) {
            try (Connection conn = ConnectionFactory.getConnection()) {
                try (PreparedStatement pstmt = conn.prepareStatement(TABLE_PHOTO_SQL_INSERT)) {
                    pstmt.setBinaryStream(1, is, is.available());// blob
                    pstmt.setString(2, picture.getFileName());
                    pstmt.setInt(3, picture.getInstituicao());
                    pstmt.setString(4, picture.getRGM());
                    pstmt.setInt(5, (int) is.available());

                    pstmt.executeUpdate();
                    conn.commit();
                }
            }
        }
    }

    public void update() throws Exception {
        try (InputStream is = picture.getBinaryStream()) {
            try (Connection conn = ConnectionFactory.getConnection()) {
                try (PreparedStatement pstmt = conn.prepareStatement(TABLE_PHOTO_SQL_UPDATE)) {
                    pstmt.setBinaryStream(1, is, is.available());// blob
                    pstmt.setInt(2, picture.getInstituicao());
                    pstmt.setString(3, picture.getRGM());
                    //pstmt.setInt(2, (int) is.available());

                    pstmt.executeUpdate();
                    conn.commit();
                }
            }
        }
    }

    /**
     * Saves an image as a File
     *
     * @param fileName the filename to save the image as
     * @throws Exception
     */
    public void saveAsFile(String fileName) throws IOException {
        ImageIO.write(picture.getBufferedImage(), picture.getType(), new File(fileName));
    }

}
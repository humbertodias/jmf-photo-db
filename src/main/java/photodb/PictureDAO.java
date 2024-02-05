package photodb;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;

public class PictureDAO {
    private Picture picture;
    private final String SQL_COUNT = "select count(*) from aluno_foto where cod_inst = ? and rgm_alun = ?";
    private final String SQL_SELECT = "select foto from aluno_foto where cod_inst = ? and rgm_alun = ?";
    private final String SQL_INSERT = "insert into aluno_foto(foto,nome_arq,cod_inst,rgm_alun,size_byte) values(?,?,?,?,?)";
    private final String SQL_UPDATE = "update aluno_foto set foto=:foto /*and dt_alt = sysdate, size_byte=:2*/ where cod_inst = :1 and rgm_alun = :2";


    public PictureDAO(Picture picture) {
        this.picture = picture;
    }

    public int count() throws Exception {
        Connection conn = ConnectionFactory.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(SQL_COUNT);
        int count = 0;

        try {

            pstmt.setInt(1, picture.getInstituicao());
            pstmt.setString(2, picture.getRGM());
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            // count
            count = rs.getInt(1);

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            if (pstmt != null)
                pstmt.close();
            if (conn != null)
                conn.close();
        }

        return count;
    }

    public BufferedImage getFoto() throws Exception {
        Connection conn = ConnectionFactory.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT);
        InputStream is = null;
        BufferedImage bi = null;
        try {
            pstmt.setInt(1, picture.getInstituicao());
            pstmt.setString(2, picture.getRGM());
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            is = rs.getBinaryStream(1);
            bi = ImageIO.read(is);
        } catch (SQLException | IOException e) {
            throw new Exception(e);
        } finally {
            if (pstmt != null)
                pstmt.close();
            if (conn != null)
                conn.close();
        }
        return bi;
    }

    public void insert() throws Exception {
        InputStream is = picture.getBinaryStream();
        Connection conn = ConnectionFactory.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT);

        try {
            pstmt.setBinaryStream(1, is, is.available());// blob
            pstmt.setString(2, picture.getFileName());
            pstmt.setInt(3, picture.getInstituicao());
            pstmt.setString(4, picture.getRGM());
            pstmt.setInt(5, (int) is.available());

            pstmt.executeUpdate();
            conn.commit();

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            if (is != null)
                is.close();
            if (pstmt != null)
                pstmt.close();
            if (conn != null)
                conn.close();
        }

    }

    public void update() throws Exception {
        InputStream is = picture.getBinaryStream();
        Connection conn = ConnectionFactory.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE);

        try {

            pstmt.setBinaryStream(1, is, is.available());// blob
            pstmt.setInt(2, picture.getInstituicao());
            pstmt.setString(3, picture.getRGM());
            //pstmt.setInt(2, (int) is.available());

            pstmt.executeUpdate();
            conn.commit();

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            if (is != null)
                is.close();
            if (pstmt != null)
                pstmt.close();
            if (conn != null)
                conn.close();
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
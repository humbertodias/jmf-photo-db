package photodb;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/*
create database jphotodb;

use jphotodb;

create table aluno_foto
(type varchar(50)
,cod_inst int
,rgm_alun varchar(50)
,nome_arq varchar(255)
,foto LONGBLOB
,size_byte int
);
*/

@Entity
@Table(name = "aluno_foto")
@XmlRootElement
public class Picture implements Serializable {
        @Column(name = "foto")
	private Image image;

        @Column(name = "type", length = 50)
	private String type = "jpg";

        @Column(name = "cod_inst")
	private int instituicao;

        @Id
        @Column(name = "rgm_alun", length = 15)
	private String rgm;
        
        
        @Column(name = "nome_arq", length = 255)
        private String nome_arq;
	

        @Column(name = "size_bytes")
	private int size_bytes;
        
        public Picture(Image image, int instituicao, String rgm, String type){
		this.image = image;
		this.instituicao = instituicao;
		this.rgm = rgm;
		this.type = type;
	}
	
	public int getInstituicao(){
		return this.instituicao;
	}
	
	public String getRGM(){
		return this.rgm;
	}

	public Image getImage(){
		return this.image;
	}
	public String getType(){
		return this.type;
	}

	public String getFileName(){
		String lpad_cod_inst = StringUtils.lpad(""+this.instituicao, '0', 7);
		String lpad_rgm_alun = StringUtils.lpad(""+this.rgm, '0', 8);
		return  lpad_cod_inst + lpad_rgm_alun + "." + this.type;
	}
	
	public ByteArrayInputStream getBinaryStream() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedImage bi = new BufferedImage(image.getWidth(null),image.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bi.createGraphics();
		g2.drawImage(image, null, null);
		ImageIO.write(bi, type, baos);
		ByteArrayInputStream biis = new ByteArrayInputStream(baos.toByteArray());
		return biis;
	}
	
	public BufferedImage getBufferedImage() {
		BufferedImage bi = new BufferedImage(image.getWidth(null),image.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bi.createGraphics();
		g2.drawImage(image, null, null);
		return bi;
	}
}

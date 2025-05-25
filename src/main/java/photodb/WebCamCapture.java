package photodb;

import javax.media.*;
import javax.media.control.FormatControl;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.util.Vector;

public class WebCamCapture extends JFrame {

    private static final Color BACKGROUND = Color.BLACK;
    private static int shotCounter = 0;
    //
    private JToolBar toolbar = new JToolBar();
    private MyToolBarAction formatButton = new MyToolBarAction("Formato");
    private MyToolBarAction captureButton = new MyToolBarAction("Capturar");
    private JPanel visualContainer = new JPanel();
    private Component visualComponent = null;
    private JLabel statusBar = null;
    //
    private CaptureDeviceInfo webCamDeviceInfo = null;
    private MediaLocator ml = null;
    private FormatControl formatControl = null;
    private Player player = null;
    //
    private MyVideoFormat currentFormat = null;
    private MyVideoFormat[] myFormatList = null;

    private int cod_inst;
    private String rgm_alun;
    private String extension = "jpg";
    private String dir = System.getProperty("java.io.tmpdir");
    private String defaultFrameSize = "200x200";
    private Dimension defaultVideoFormatSize = new Dimension(352, 288);

    /**
     * Constructor
     *
     * @param frameTitle
     * @param cod_inst
     * @param rgm_alun
     */
    public WebCamCapture(String frameTitle, int cod_inst, String rgm_alun) {
        super(frameTitle + " : " + cod_inst + "-" + rgm_alun);

        this.cod_inst = cod_inst;
        this.rgm_alun = rgm_alun;

        setSize(330, 320); // default size...
        //
        statusBar = new JLabel("Initializing...") {
            // Nasty bug workaround
            // The minimum JLabel size was determined by the text in the status
            // bar
            // So the layoutmanager wouldn't shrink the window for the video
            // image
            @Override
            public Dimension getPreferredSize() {
                // get the JLabel to "allow" a minimum of 10 pixels width
                // not to work out the minimum size from the text length
                return (new Dimension(10, (int) super.getPreferredSize().getHeight()));
            }
        };
        statusBar.setBorder(new EtchedBorder());
        //
        visualContainer.setLayout(new BorderLayout());
        visualContainer.setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        add(visualContainer, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        addListener();
        // the center of screen
        this.setLocationRelativeTo(null);
    }

    protected void addListener() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
    }

    /**
     * Initialise
     *
     * @returns true if webcam is detected
     */
    public boolean initialise() {
        MyCaptureDeviceInfo[] cams = autoDetect();
        //
        if (cams.length > 0) {
            if (cams.length == 1) {
                System.out.println("Note : 1 web cam detected");
                return (initialise(cams[0].capDevInfo));
            } else {
                System.out.println("Note : " + cams.length + " web cams detected");
                Object selected = JOptionPane.showInputDialog(this,
                        "Select Video format", "Capture format selection",
                        JOptionPane.INFORMATION_MESSAGE, null, // Icon icon,
                        cams, // videoFormats,
                        cams[0]);
                //
                if (selected != null) {
                    return (initialise(((MyCaptureDeviceInfo) selected).capDevInfo));
                } else {
                    return (initialise(null));
                }
            }
        } else {
            return (initialise(null));
        }
    }

    /**
     * Initialise
     *
     * @param _deviceInfo, specific web cam device if not autodetected
     * @return true if webcam is detected
     */
    public boolean initialise(CaptureDeviceInfo _deviceInfo) {
        setStatusBar("Initialising...");
        webCamDeviceInfo = _deviceInfo;

        if (webCamDeviceInfo != null) {
            setStatusBar("Connecting to : " + webCamDeviceInfo.getName());
            try {
                setUpToolBar();
                getContentPane().add(toolbar, BorderLayout.NORTH);
                //
                ml = webCamDeviceInfo.getLocator();
                if (ml != null) {
                    player = Manager.createRealizedPlayer(ml);
                    if (player != null) {
                        player.start();
                        //
                        setStatusBar("Connected: " + webCamDeviceInfo.getName());

                        formatControl = (FormatControl) player.getControl("javax.media.control.FormatControl");
                        Format[] formats = webCamDeviceInfo.getFormats();
                        //
                        myFormatList = new MyVideoFormat[formats.length];
                        for (int i = 0, j = 0; i < formats.length; i++) {
                            if (formats[i] instanceof VideoFormat) {
                                myFormatList[j++] = new MyVideoFormat((VideoFormat) formats[i]);
                            }
                        }
                        //clean
                        formats = null;

                        //
                        Format currFormat = formatControl.getFormat();

                        //Default Video Format
                        VideoFormat cvf = (VideoFormat) currFormat;
                        currFormat = new VideoFormat(cvf.getEncoding(), defaultVideoFormatSize, cvf.getMaxDataLength(), cvf.getDataType(), cvf.getFrameRate()
                        );
                        visualContainer.setPreferredSize(defaultVideoFormatSize);
                        //Default Video Format

                        //
                        visualComponent = player.getVisualComponent();
                        if (visualComponent != null) {
                            visualContainer.add(visualComponent, BorderLayout.CENTER);

                            if (currFormat instanceof VideoFormat) {
                                this.currentFormat = new MyVideoFormat((VideoFormat) currFormat);
                            } else {
                                setStatusBar("Error : Can not get current video format");
                                return (true);
                            }

                            //
                            invalidate();
                            pack();
                            return (true);
                        } else {
                            setStatusBar("Error : Could not get visual component");
                            return (false);
                        }
                    } else {
                        setStatusBar("Error : Cannot create player");
                        setStatusBar("Cannot create player");
                        return (false);
                    }
                } else {
                    setStatusBar("Error : No MediaLocator for " + webCamDeviceInfo.getName());
                    setStatusBar("No Media Locator for : " + webCamDeviceInfo.getName());
                    return (false);
                }
            } catch (IOException ioEx) {
                setStatusBar("Error connecting to [" + webCamDeviceInfo.getName() + "] : " + ioEx.getMessage());
                setStatusBar("Connecting to : " + webCamDeviceInfo.getName());
                return (false);
            } catch (NoPlayerException npex) {
                setStatusBar("Cannot create player");
                return (false);
            } catch (CannotRealizeException nre) {
                setStatusBar("Cannot realize player");
                return (false);
            }
        }
        return (false);
    }

    public void setStatusBar(String text) {
        statusBar.setText(text);
        statusBar.setToolTipText(text);
    }

    /**
     * Dynamically create menu items
     *
     * @param selectedFormat the device info object if found, null otherwise
     */
    public void setFormat(MyVideoFormat selectedFormat) {
        if (formatControl != null) {
            player.stop();

            if (visualComponent != null) {
                visualContainer.remove(visualComponent);
            }

            this.currentFormat = selectedFormat;
            this.visualContainer.setPreferredSize(currentFormat.getVideoFormat().getSize());
            formatControl.setFormat(currentFormat.getVideoFormat());
            //
            setStatusBar("Format : " + currentFormat);
            //
            player.start();

            visualComponent = player.getVisualComponent();
            if (visualComponent != null) {
                visualContainer.add(visualComponent, BorderLayout.CENTER);
            }

            invalidate(); // let the layout manager work out the sizes
            pack();
        } else {
            setStatusBar("Visual component cannot change format");
        }
    }

    private void setUpToolBar() {
        // Note : due to cosmetic glitches when undocking and docking the
        // toolbar,
        // I've set this to false.
        toolbar.setFloatable(false);
        // Note : If you supply the 16 x 16 bitmaps then you can replace
        // the commented line in the MyToolBarAction constructor
        toolbar.add(formatButton);
        toolbar.add(captureButton);

        //
        add(toolbar, BorderLayout.NORTH);
    }

    private void toolbarHandler(MyToolBarAction actionBtn) {
        if (actionBtn == formatButton) {
            Object selected = JOptionPane.showInputDialog(this,
                    "Select Video format", "Capture format selection",
                    JOptionPane.INFORMATION_MESSAGE, null, // Icon icon,
                    myFormatList, // videoFormats,
                    currentFormat);
            if (selected != null) {
                setFormat((MyVideoFormat) selected);
            }
        } else if (actionBtn == captureButton) {
            BufferedImage bfi = (BufferedImage) grabFrameImage();
            Image image = getResizedImage(bfi, currentFormat.getVideoFormat().getSize());
            if (image != null) {
                new MySnapshot(image);
            } else {
                setStatusBar("Error : Could not grab frame");
            }
        }
    }

    /**
     * AutoDetects the first web camera in the system searches for video for
     * windows ( vfw ) and linux ( v4l ) capture devices
     *
     * @return the device info object if found, null otherwise
     */
    public MyCaptureDeviceInfo[] autoDetect() {
        Vector<CaptureDeviceInfo> list = CaptureDeviceManager.getDeviceList(null);
        Vector<MyCaptureDeviceInfo> cams = new Vector<>();
        if (list != null) {
            for (CaptureDeviceInfo devInfo : list) {
                String name = devInfo.getName();
                // v4l:linux || vfw:windows || another (solaris) for example, I'm not sure about there syntax
                if (name.toLowerCase().startsWith("v")) {
                    cams.addElement(new MyCaptureDeviceInfo(devInfo));
                }
            }// of for
        }
        MyCaptureDeviceInfo[] detected = new MyCaptureDeviceInfo[cams.size()];
        for (int i = 0; i < cams.size(); i++) {
            detected[i] = cams.elementAt(i);
        }
        return (detected);
    }//of autoDetect

    /**
     * grabs a frame's buffer from the webcam / device
     *
     * @return A frames buffer
     */
    public Buffer grabFrameBuffer() {
        Buffer buf = null;
        if (player != null) {
            FrameGrabbingControl fgc = null;
            fgc = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");
            buf = fgc.grabFrame();
            if (fgc != null) {
                return (buf);
            } else {
                setStatusBar("Error : FrameGrabbingControl is null");
                return buf;
            }
        } else {
            setStatusBar("Error : Player is null");
            return buf;
        }
    }//of grabFrameBuffer

    /**
     * grabs a frame's buffer, as an image, from the webcam / device
     *
     * @return A frames buffer as an image
     */
    public Image grabFrameImage() {
        Buffer buffer = grabFrameBuffer();
        if (buffer != null) {
            // Convert it to an image
            BufferToImage btoi = new BufferToImage((VideoFormat) buffer.getFormat());
            if (btoi != null) {
                Image image = btoi.createImage(buffer);
                if (image != null) {
                    return (image);
                } else {
                    setStatusBar("Error : BufferToImage cannot convert buffer");
                }
            } else {
                setStatusBar("Error : Cannot create BufferToImage instance");
            }
        } else {
            setStatusBar("Error : Buffer grabbed is null");
        }
        return null;
    }//of grabFraneImage

    /**
     * Resize the BufferedImage according to Dimension d
     *
     * @param image
     * @param d
     * @return
     */
    public Image getResizedImage(BufferedImage image, Dimension d) {
        BufferedImage results = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = results.createGraphics();
        double scaleX = d.width / (double) image.getWidth(null);
        double scaleY = d.height / (double) image.getHeight(null);
        AffineTransform xform = AffineTransform.getScaleInstance(scaleX, scaleY);
        g2.drawRenderedImage(image, xform);
        g2.dispose();
        return results;
    }//of getResizedImage

    /**
     * Closes and cleans up the player
     */
    public void playerClose() {
        if (player != null) {
            player.close();
            player.deallocate();
            player = null;
        }
    }

    public void exit() {
        System.out.println("Closing...");
        playerClose();
        System.exit(1);
    }

    class MyToolBarAction extends AbstractAction {

        public MyToolBarAction(String name, String imagefile) {
            // Note : Use version this if you supply your own toolbar icons
            super(name, new ImageIcon(imagefile));
        }

        public MyToolBarAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            toolbarHandler(this);
        }
    }// of MyToolBarAction

    class MyVideoFormat {

        private VideoFormat format;
        private int bps = -1;

        public MyVideoFormat(VideoFormat format) {
            this.format = format;
            if (this.format instanceof RGBFormat) {
                bps = ((RGBFormat) this.format).getBitsPerPixel();
            }
        }

        public VideoFormat getVideoFormat() {
            return this.format;
        }

        @Override
        public String toString() {
            Dimension dim = format.getSize();
            return (format.getEncoding()
                    + " [ " + dim.width + " x " + dim.height + " ] "
                    + " : " + bps + "bps");
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MyVideoFormat) {
                VideoFormat vf = ((MyVideoFormat) o).format;
                if (vf instanceof RGBFormat) {
                    RGBFormat rgb = (RGBFormat) vf;
                    return rgb.getEncoding().equals(this.format.getEncoding())
                            && rgb.getSize().equals(this.format.getSize())
                            && rgb.getBitsPerPixel() == this.bps;
                } else {
                    return vf.getEncoding().equals(this.format.getEncoding())
                            && vf.getSize().equals(this.format.getSize());
                }
            }
            return false;
        }
    }// of MyVideoFormat

    class MyCaptureDeviceInfo {

        private CaptureDeviceInfo capDevInfo;

        public MyCaptureDeviceInfo(CaptureDeviceInfo devInfo) {
            capDevInfo = devInfo;
        }

        @Override
        public String toString() {
            return (capDevInfo.getName());
        }
    }// of MyCaptureDeviceInfo

    class MySnapshot extends JFrame implements ImageObserver {

        private CanvasImage canvas = new CanvasImage();

        public MySnapshot(Image image) {
            shotCounter++;
            canvas.setImage(image);
            try {

                //qdo pressionar o botao do mouse, capturar
                canvas.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent me) {
                        capture();
                    }
                });
                //qdo mover o mouse, mover o frame de enquadramento
                canvas.addMouseMotionListener(new MouseAdapter() {
                    public void mouseMoved(MouseEvent me) {
                        canvas.moveFrame(me.getX(), me.getY());
                        setTitlePosition();
                    }
                });

                String defaultSize = image.getWidth(this) < 185 ? image.getWidth(this) + "x" + image.getHeight(this) : defaultFrameSize;
                //String frameSize = JOptionPane.showInputDialog("Tamanho do Quadro", defaultSize);
                //no pergunte, fique o quadro
                String frameSize = defaultSize;
                if (frameSize != null) {
                    String[] fs = frameSize.toLowerCase().split("x");
                    canvas.setFrame(new Rectangle(0, 0, Integer.parseInt(fs[0]), Integer.parseInt(fs[1])));

                    add(canvas);
                    //
                    addListener();
                    //center
                    setIconImage(canvas.image);
                    pack();
                    setLocationRelativeTo(null);
                    setVisible(true);
                    setResizable(false);//not resizable
                    setTitlePosition();
                }
            } catch (Exception ex) {
                showException(this, "FrameSize error", ex);
            }
        }

        protected void addListener() {
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    setTitlePosition();
                }
            });
            //
            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            canvas.addLeft();
                            break;
                        case KeyEvent.VK_RIGHT:
                            canvas.addRight();
                            break;
                        case KeyEvent.VK_UP:
                            canvas.addUp();
                            break;
                        case KeyEvent.VK_DOWN:
                            canvas.addDown();
                            break;
                        case KeyEvent.VK_ENTER:
                            capture();
                            //dispose();//close
                            break;
                        case KeyEvent.VK_ESCAPE:
                            dispose();
                    }// end switch
                    //
                    setTitlePosition();
                    canvas.repaint();
                }// end keyPressed
            });
        }

        public void capture() {
            String msg_dao = "", msg_fs = "";
            PictureDAO dao = null;
            Picture p = null;
            int answer_update = JOptionPane.OK_OPTION;

            try {
                BufferedImage bi = canvas.getBufferedImage();

                //dao
                p = new Picture(bi, cod_inst, rgm_alun, extension);
                dao = new PictureDAO(p);

                //se houver atualize
                if (dao.count() > 0) {
                    //-------
                    String msg = "<html><boyd>Já existe uma foto para o aluno " + cod_inst + "-" + rgm_alun
                            + "<br/>Gostaria de sobreescrevela?</body></html>";

                    JPanel pAnterior = new JPanel();
                    pAnterior.setBorder(BorderFactory.createTitledBorder("Anterior"));
                    pAnterior.add(new JLabel(new ImageIcon(dao.getFoto())));

                    JPanel pAtual = new JPanel();
                    TitledBorder bRight = BorderFactory.createTitledBorder("Atual");
                    bRight.setTitleJustification(TitledBorder.RIGHT);
                    pAtual.setBorder(bRight);
                    pAtual.add(new JLabel(new ImageIcon(bi)));

                    JPanel jp = new JPanel();

                    jp.setLayout(new BorderLayout());
                    jp.add(pAnterior, BorderLayout.WEST);
                    jp.add(pAtual, BorderLayout.EAST);
                    jp.add(new JLabel(msg), BorderLayout.SOUTH);

                    answer_update = JOptionPane.showConfirmDialog(this, jp, "Atualização de Foto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    //-----
                    if (answer_update == JOptionPane.OK_OPTION) {
                        dao.update();
                    } else {
                        dispose();
                    }

                }//if
                //senao insira
                else {
                    dao.insert();
                }//else

                msg_dao = "1 - Sucesso ao Incluir/Atualizar foto no Banco de dados.";

            } catch (Exception ex) {
                msg_dao = "1 - Erro ao incluir/alterar Foto no Banco de dados.";
                showException(this, "Erro ao gravar foto no banco de dados", ex);
            }

            if (answer_update == JOptionPane.OK_OPTION) {

                try {
                    //fs
                    String filename_fullpath = dir + File.separator + p.getFileName();
                    dao.saveAsFile(filename_fullpath);
                    msg_fs = "2 - Sucesso ao Incluir foto em " + dir;
                    //fs

                } catch (Exception ex) {
                    msg_fs = "2 - Erro ao incluir foto em " + dir;
                    showException(this, "Erro ao gravar foto em " + dir, ex);
                }

                String msg = msg_dao
                        + "\n" + msg_fs
                        + "\nGostaria de reenquadrar a última foto?";
                String titulo = "Resultado da gravaçao de foto para o Aluno : " + cod_inst + "-" + rgm_alun;
                int answer = JOptionPane.showConfirmDialog(MySnapshot.this, msg, titulo, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.NO_OPTION) {
                    dispose();
                }

            }
        }

        protected void setTitlePosition() {
            Rectangle frame = canvas.getFrame();
            if (frame.x == 0 && frame.y == 0) {
                setTitle("Click/Enter CAPTURA | Seta/Mouse ENQUADRA");
            } else {
                setTitle("Enquadrando Foto(" + shotCounter + ")-"
                        /*+ "{" + cod_inst + "-" + rgm_alun + "}"*/
                        + "[" + frame.x + "x" + frame.y + "]"
                        + "[" + frame.width + "x" + frame.height + "]");
            }
        }

        protected void exit() {
            setVisible(false);
            dispose();
        }

        class CanvasImage extends Canvas {

            private Image image;
            private Rectangle frame;
            private Color frameColor = Color.WHITE;

            public CanvasImage() {
            }

            public void moveFrame(int x, int y) {
                frame.x = x;
                frame.y = y;
                //System.out.println("(x,y): " + me.getX() + "," + me.getY());
                repaint();
            }

            public CanvasImage(Image image) {
                setImage(image);
            }

            public Image getImage() {
                return image;
            }

            public Rectangle getFrame() {
                return frame;
            }
            //

            @Override
            public void paint(Graphics g) {
                if (image != null) {
                    g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                    paintFrame(g);
                }
            }

            public void paintFrame(Graphics g) {
                if (g != null && frame != null) {
                    g.setColor(frameColor);
                    g.drawRect(frame.x, frame.y, frame.width, frame.height);
                }
            }

            @Override
            public void update(Graphics g) {
                g.drawImage(image, 0, 0, null);
                paintFrame(g);
            }

            public void setImage(Image image) {
                this.image = image;
                setFrame(new Rectangle(0, 0, getImageWidth(), getImageHeight()));
                setSize(frame.getSize());
            }

            public void setFrame(Rectangle r) {
                this.frame = r;
            }

            public int getImageWidth() {
                return this.image.getWidth(null);
            }

            public int getImageHeight() {
                return this.image.getHeight(null);
            }

            public BufferedImage getBufferedImage() throws Exception {
                return ((BufferedImage) image).getSubimage(frame.x, frame.y, frame.width, frame.height);
            }

            public void addRight() {
                frame.x++;
            }

            public void addLeft() {
                frame.x--;
            }

            public void addUp() {
                frame.y--;
            }

            public void addDown() {
                frame.y++;
            }

        }// of CanvasImage

    }// of MySnapshot

    public static void showException(Component parent, String title, Exception ex) {
        final Writer sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        //out tb
        System.err.println(sw);

        JScrollPane sp = new JScrollPane(new JTextArea(sw.toString()));
        sp.setPreferredSize(new Dimension(450, 200));
        JOptionPane.showMessageDialog(parent, sp, title, JOptionPane.ERROR_MESSAGE);
    }

}

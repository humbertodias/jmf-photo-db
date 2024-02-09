package photodb;


import javax.swing.*;

import static photodb.WebCamCapture.showException;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                throw new IllegalArgumentException("Usage:\njava -jar photo-db.jar <cod_inst> <rgm_alun>");
            } else {
                String cod_inst = args[0];
                String rgm_alun = args[1];

                WebCamCapture myWebCam = new WebCamCapture("JFoto para o aluno ", Integer.parseInt(cod_inst), rgm_alun);
                myWebCam.setVisible(true);
                //
                if (!myWebCam.initialise()) {
                    myWebCam.setStatusBar("Web Cam not detected / initialised");
                    //
                    if (System.getProperty("os.name").equals("Linux")) {
                        JOptionPane.showMessageDialog(null, "Don't forget to export the variable LD_LIBRARY_PATH"
                                + "\nbefore to run. Please try this one:"
                                + "\n\nexport LD_LIBRARY_PATH=/opt/jmf/lib", "LD_LIBRARY_PATH", JOptionPane.WARNING_MESSAGE);
                    }
                }

            }

        } catch (Exception ex) {
            showException(null, "Error initializing", ex);
        }
    }
}

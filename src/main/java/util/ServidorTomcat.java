package util;

import java.io.File;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/**
 * Servidor Tomcat Embutido para inicialização instantânea da aplicação Web
 * sem necessidade de instalação manual de servidor ou IDE específica.
 * Executável via 'mvn compile exec:java'.
 */
public class ServidorTomcat {

    public static void main(String[] args) throws Exception {
        int porta = 8080;
        String portaEnv = System.getenv("PORT");
        if (portaEnv != null && !portaEnv.trim().isEmpty()) {
            try {
                porta = Integer.parseInt(portaEnv.trim());
            } catch (NumberFormatException ignored) {}
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(porta);
        tomcat.setBaseDir("target/tomcat");
        tomcat.getConnector();

        String webappDirLocation = new File("src/main/webapp").getAbsolutePath();
        StandardContext ctx = (StandardContext) tomcat.addWebapp("", webappDirLocation);
        ctx.setParentClassLoader(ServidorTomcat.class.getClassLoader());

        File additionWebInfClasses = new File("target/classes");
        if (additionWebInfClasses.exists()) {
            WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }

        System.out.println("------------------------------------------------------------------");
        System.out.println(" POUSADA RESERVAS - AVALIAÇÃO M1 ENGENHARIA DE SOFTWARE (UMC)     ");
        System.out.println(" Aluno: Marco Antonio Lopes Pedro - Grupo G14                     ");
        System.out.println(" Servidor ativo em: http://localhost:" + porta + "/controller.do   ");
        System.out.println("------------------------------------------------------------------");

        tomcat.start();
        tomcat.getServer().await();
    }
}

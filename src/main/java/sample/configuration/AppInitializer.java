package sample.configuration;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{AppConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        FilterRegistration.Dynamic fr = container.addFilter("encodingFilter",
                new CharacterEncodingFilter());
        fr.setInitParameter("encoding", "UTF-8");
        fr.setInitParameter("forceEncoding", "true");
        fr.addMappingForUrlPatterns(null, true, "/*");

        super.onStartup(container);
    }


    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setMultipartConfig(getMultipartConfigElement());
    }

    private MultipartConfigElement getMultipartConfigElement() {
        // default values
        long maxFileSize = 10 * 1024 * 1024;
        long maxRequestSize = maxFileSize * 2;
        int fileSizeThreshold = 0;
        try {
            Properties paths = new Properties();
            paths.load(new FileInputStream(getClass().getClassLoader().getResource("paths.properties").getFile()));
            if (!paths.containsKey("application")) {
                throw new IOException("No path for application properties in paths.properties");
            }

            Properties application = new Properties();
            application.load(new FileInputStream(paths.getProperty("application")));
            maxFileSize = Long.parseLong(application.getProperty("file.max_size.bytes", Long.toString(maxFileSize)));
            maxRequestSize = maxFileSize * 2;
        } catch (IOException e) {
            logger.warn("Failed to get spring file parameters", e);
        }

        return new MultipartConfigElement(LOCATION, maxFileSize, maxRequestSize, fileSizeThreshold);
    }

    private static final String LOCATION = "/tmp";
}

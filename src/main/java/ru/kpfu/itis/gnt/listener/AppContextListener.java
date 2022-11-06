package ru.kpfu.itis.gnt.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.kpfu.itis.gnt.DAO.implementations.CommentsRepositoryImpl;
import ru.kpfu.itis.gnt.DAO.implementations.DataSource;
import ru.kpfu.itis.gnt.DAO.implementations.PostsRepositoryImpl;
import ru.kpfu.itis.gnt.DAO.implementations.UsersRepositoryJDBCTemplateImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    private DriverManagerDataSource dataSource;
    private UsersRepositoryJDBCTemplateImpl userDAO;

    private PostsRepositoryImpl postDao;

    private CommentsRepositoryImpl commentsDao;
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        dataSource = new DataSource().getDataSource();
        userDAO = new UsersRepositoryJDBCTemplateImpl(dataSource);
        postDao = new PostsRepositoryImpl(dataSource);
        commentsDao = new CommentsRepositoryImpl(dataSource);

        servletContextEvent.getServletContext().setAttribute("POSTS_DAO", postDao);
        servletContextEvent.getServletContext().setAttribute("COMMENTS_DAO", commentsDao);
        servletContextEvent.getServletContext().setAttribute("USERDAO", userDAO);
        servletContextEvent.getServletContext().setAttribute("CONNECTION", dataSource);
        servletContextEvent.getServletContext().setAttribute("OBJECT_MAPPER", new ObjectMapper());
    }
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}

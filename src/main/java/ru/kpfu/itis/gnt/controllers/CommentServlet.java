package ru.kpfu.itis.gnt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.kpfu.itis.gnt.entities.Comment;
import ru.kpfu.itis.gnt.entities.CommentObject;
import ru.kpfu.itis.gnt.entities.Post;
import ru.kpfu.itis.gnt.entities.User;
import ru.kpfu.itis.gnt.exceptions.DBException;
import ru.kpfu.itis.gnt.services.UsersService;
import ru.kpfu.itis.gnt.services.implementations.CommentsServiceImpl;
import ru.kpfu.itis.gnt.services.implementations.PostsServiceImpl;
import ru.kpfu.itis.gnt.services.implementations.UsersAuthenticationService;
import ru.kpfu.itis.gnt.services.implementations.UsersServiceImpl;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


@WebServlet("/comment")
public class CommentServlet extends HttpServlet {
    private int postId;


    private String commentBody;
    private PostsServiceImpl postsService;
    private CommentsServiceImpl commentsService;

    private List<ru.kpfu.itis.gnt.entities.Comment> commentList;

    private UsersAuthenticationService usersService;
    private User postAuthor;

    private User commentAuthor;
    private Post post;

    private int commentAuthorId;

    @Override
    public void init() {
        postsService = new PostsServiceImpl(getServletContext());
        commentsService = new CommentsServiceImpl(getServletContext());
        usersService = new UsersAuthenticationService(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            initValues(req);
            ObjectMapper objectMapper = (ObjectMapper) getServletContext().getAttribute("OBJECT_MAPPER");
            addCommentToDB(resp, objectMapper,req);
        } catch (NumberFormatException | DBException e) {
            throw new RuntimeException(e);
        }
    }

    private void addCommentToDB(HttpServletResponse resp, ObjectMapper objectMapper, HttpServletRequest req) {

        try {
            Comment commentServletToDb = new Comment(
                    commentBody,
                    postId,
                    commentAuthorId
            );

            // если комментарий успешно добавился в бд,
            // то тогда создаём новый объект комментария и передаем на фронт
            // с помощью json
            if (commentsService.addComment(commentServletToDb)) {
                commentList = commentsService.getAllComments(post);
                // отображаем только последний
                Comment comment = commentList.get(commentList.size() - 1);
                commentAuthor = usersService.findUserById(comment.getAuthor_id());

                CommentObject commentToDisplay = new CommentObject(
                        commentBody,
                        postId,
                        commentAuthor.getFirstName() + " " + commentAuthor.getLastName(),
                        comment.getCreated_at()
                );
                String jsonResponse = objectMapper.writeValueAsString(commentToDisplay);
                resp.setContentType("application/json");
                resp.getWriter().write(jsonResponse);
            }
        } catch (IOException | DBException | NumberFormatException ex) {
            System.out.println(ex);
        }
    }


    private void initValues(HttpServletRequest req) throws DBException, NumberFormatException {
        postId = Integer.parseInt(req.getParameter("postIndex"));
        commentBody = req.getParameter("comment");
        post = postsService.getPostById(postId);
        postAuthor = postsService.getPostAuthor(post);
        commentAuthorId = (int) req.getSession().getAttribute("USER_ID");
    }
}

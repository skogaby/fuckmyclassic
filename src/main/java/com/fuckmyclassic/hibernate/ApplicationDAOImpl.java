package com.fuckmyclassic.hibernate;

import com.fuckmyclassic.model.Application;
import com.fuckmyclassic.model.Folder;
import com.fuckmyclassic.model.LibraryItem;
import com.fuckmyclassic.shared.SharedConstants;
import javafx.scene.control.TreeItem;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the ApplicationDAO interface.
 */
@Repository
public class ApplicationDAOImpl implements ApplicationDAO {

    /**
     * Hibernate manager for higher level interactions.
     */
    private final HibernateManager hibernateManager;

    /**
     * Hibernate session for database interaction at a low level.
     */
    private final Session session;

    @Autowired
    public ApplicationDAOImpl(final HibernateManager hibernateManager, final Session session) {
        this.hibernateManager = hibernateManager;
        this.session = session;
    }

    @Override
    public Application loadApplicationById(final String applicationId) {
        final Query<Application> query = session.createQuery("from Application where application_id = :id");
        query.setParameter("id", applicationId);
        final List<Application> results = query.getResultList();
        Application app = null;

        if (results != null && !results.isEmpty()) {
            app = results.get(0);
        }

        return app;
    }

    @Override
    public List<TreeItem<Application>> loadApplications(List<String> applicationIds) {
        final Query<Application> query = session.createQuery("from Application where applicatison_id in (:ids)");
        query.setParameterList("ids", applicationIds);
        return query.getResultStream().map(r -> new TreeItem<>(r)).collect(Collectors.toList());
    }

    @Override
    public void loadApplicationsForFolder(TreeItem<Application> parentFolder, String consoleSid, int libraryId) {
        // get all the applications in the top-level folder
        final Query<LibraryItem> query = session.createQuery("from LibraryItem l where l.consoleSid = :console_sid and l.libraryId = :library_id and l.folder = :folder");
        query.setParameter("console_sid", consoleSid);
        query.setParameter("library_id", libraryId);
        query.setParameter("folder", parentFolder.getValue());

        final List<TreeItem<Application>> itemResults = query.getResultStream()
                .map(r -> new TreeItem<Application>(r.getApplication()))
                .collect(Collectors.toList());
        parentFolder.getChildren().addAll(itemResults);

        // iterate through the results and recurse down for any folders that are inside this one
        for (TreeItem<Application> itemResult : itemResults) {
            if (itemResult.getValue() instanceof Folder) {
                loadApplicationsForFolder(itemResult, consoleSid, libraryId);
            }
        }
    }

    @Override
    public TreeItem<Application> loadLibraryForConsole(String consoleSid, int libraryId) {
        // check if the HOME folder exists, create one if it doesn't
        Application homeFolder = loadApplicationById(SharedConstants.HOME_FOLDER_ID);

        if (homeFolder == null) {
            homeFolder = new Folder();
            homeFolder.setApplicationName(SharedConstants.HOME_FOLDER_NAME);
            homeFolder.setApplicationId(SharedConstants.HOME_FOLDER_ID);
            hibernateManager.saveEntity(homeFolder);
        }

        // now select all items in the home folder and recurse down for any folders
        final TreeItem<Application> homeItem = new TreeItem<>(homeFolder);
        homeItem.setExpanded(true);
        loadApplicationsForFolder(homeItem, consoleSid, libraryId);

        return homeItem;
    }
}

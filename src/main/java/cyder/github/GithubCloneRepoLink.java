package main.java.cyder.github;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.network.NetworkUtil;
import main.java.cyder.strings.CyderStrings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A container for a github link to clone a repository.
 */
@Immutable
public class GithubCloneRepoLink {
    /**
     * The regex to extract the user and repo name from a valid github .git repo link.
     */
    private static final String githubRepoCloneRegex = ".*github\\.com/(.*)/(.*)\\.git";

    /**
     * The compiled pattern matcher for {@link #githubRepoCloneRegex}.
     */
    private static final Pattern githubRepoClonePattern = Pattern.compile(githubRepoCloneRegex);

    /**
     * The .git link for the github repository.
     */
    private final String link;

    /**
     * The user/organization the repository belongs to.
     */
    private final String user;

    /**
     * The repository name.
     */
    private final String repository;

    /**
     * Suppress default constructor.
     */
    private GithubCloneRepoLink() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Constructs a new github clone repo link.
     *
     * @param link the https clone link
     */
    public GithubCloneRepoLink(String link) {
        Preconditions.checkNotNull(link);
        Preconditions.checkArgument(!link.isEmpty());

        Matcher matcher = githubRepoClonePattern.matcher(link);
        Preconditions.checkArgument(matcher.matches());

        this.link = link;
        this.user = matcher.group(1);
        this.repository = matcher.group(2);
    }

    /**
     * Returns the raw link.
     *
     * @return the raw link
     */
    public String getLink() {
        return link;
    }

    /**
     * Returns the owner of this repo.
     *
     * @return the owner of this repo
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the repository name.
     *
     * @return the repository name
     */
    public String getRepository() {
        return repository;
    }

    /**
     * Returns whether the link actually exists.
     *
     * @return whether the link actually exists
     */
    public boolean urlExists() {
        boolean ret;

        try {
            String contents = NetworkUtil.readUrl(link);
            ret = !contents.isEmpty();
        } catch (Exception ignored) {
            ret = false;
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GithubCloneRepoLink{"
                + "link="
                + CyderStrings.quote
                + link
                + CyderStrings.quote
                + ", user="
                + CyderStrings.quote
                + user
                + CyderStrings.quote
                + ", repository="
                + CyderStrings.quote
                + repository
                + CyderStrings.quote
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof GithubCloneRepoLink)) {
            return false;
        }

        GithubCloneRepoLink other = (GithubCloneRepoLink) o;
        return link.equals(other.getLink());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return link.hashCode();
    }
}

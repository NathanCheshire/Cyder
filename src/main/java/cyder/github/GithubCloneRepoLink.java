package cyder.github;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import cyder.exceptions.IllegalMethodException;
import cyder.network.NetworkUtil;
import cyder.strings.CyderStrings;

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
    private static final String githubRepoCloneRegex = "^((http|https)://)?(www\\.)?github\\.com/(.*)/(.*)\\.git";

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
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
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

        this.link = correctCloneLink(link);

        this.user = matcher.group(4);
        this.repository = matcher.group(5);
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
        try {
            return !NetworkUtil.readUrl(link).isEmpty();
        } catch (Exception ignored) {}

        return false;
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

    /**
     * Corrects the provided github clone link to ensure it begins with https://www.github.com.
     *
     * @param link the provided clone link which is valid but not in proper form
     * @return the corrected link
     */
    private static String correctCloneLink(String link) {
        Preconditions.checkNotNull(link);
        Preconditions.checkArgument(!link.isEmpty());

        StringBuilder ret = new StringBuilder(link);

        // Insert www if starts with github.com
        if (!link.startsWith("www") && !link.startsWith("https://") && !link.startsWith("http://")) {
            ret.insert(0, "www.");
        }

        // Insert https if http not present
        if (!link.startsWith("https://") && !link.startsWith("http://")) {
            ret.insert(0, "https://");
        }

        // Convert non-safe to safe
        if (ret.toString().startsWith("http") && !ret.toString().startsWith("https")) {
            ret.insert(4, "s");
        }

        String[] parts = ret.toString().split("://");
        String protocol = parts[0];
        String domainAndRemainingUrl = parts[1];

        // Ensure www precedes github.com
        if (!domainAndRemainingUrl.startsWith("www")) {
            domainAndRemainingUrl = "www." + domainAndRemainingUrl;
        }

        return protocol + "://" + domainAndRemainingUrl;
    }
}

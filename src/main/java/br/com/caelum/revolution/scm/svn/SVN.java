package br.com.caelum.revolution.scm.svn;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import br.com.caelum.revolution.changesets.ChangeSet;
import br.com.caelum.revolution.domain.ArtifactKind;
import br.com.caelum.revolution.domain.ModificationKind;
import br.com.caelum.revolution.scm.CommitData;
import br.com.caelum.revolution.scm.DiffData;
import br.com.caelum.revolution.scm.SCM;
import br.com.caelum.revolution.scm.SCMException;
import br.com.caelum.revolution.util.io.IOUtils;

public class SVN implements SCM {

	private final String repoPath;
	private SVNRepository repository;
	private String username;
	private String password;
	private final SVNDiffParser diffParser;
	private final String tempPath;
	private final IOUtils io;
	private String sourceCodePath;

	public SVN(String path, String tempPath, SVNDiffParser diffParser, IOUtils io) {
		this.tempPath = tempPath;
		this.diffParser = diffParser;
		this.io = io;
		setup();

		this.repoPath = path;
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(path));
		} catch (SVNException e) {
			throw new SCMException(e);
		}
	}

	public SVN(String path, String tempPath, String username, String password, SVNDiffParser diffParser, IOUtils io) {
		setup();

		this.io = io;
		this.tempPath = tempPath;
		this.diffParser = diffParser;
		this.username = username;
		this.password = password;
		this.repoPath = path;
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(path));
			repository.setAuthenticationManager(createAuthManager());
		} catch (SVNException e) {
			throw new SCMException(e);
		}
	}

	private ISVNAuthenticationManager createAuthManager() {
		return SVNWCUtil.createDefaultAuthenticationManager(username, password);
	}

	@SuppressWarnings({ "unchecked" })
	public List<ChangeSet> getChangeSets() {
		List<ChangeSet> cs = new ArrayList<ChangeSet>();

		try {
			List<SVNLogEntry> log = (List<SVNLogEntry>) repository.log(null, null, 0, -1, true, true);

			System.out.println("tamanho da lista " + log.size());
			for (SVNLogEntry entry : log) {
				cs.add(new ChangeSet(String.valueOf(entry.getRevision()), convert(entry.getDate())));
			}

		} catch (SVNException e) {
			throw new SCMException(e);
		}

		return cs;
	}

	private Calendar convert(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	public String goTo(String id) {
		try {
			String destinationPath = tempPath;
			io.removeDirectory(new File(destinationPath));
			
			SVNUpdateClient updateClient = SVNClientManager.newInstance(null, repository.getAuthenticationManager()).getUpdateClient();
			updateClient.setIgnoreExternals( false );
			System.out.println("checking out id " + id);
			updateClient.doCheckout(SVNURL.parseURIDecoded(repoPath), new File(destinationPath) , SVNRevision.create(Long.parseLong(id)), SVNRevision.create(Long.parseLong(id)), SVNDepth.INFINITY, true);
			System.out.println("done in " + destinationPath);
			
			sourceCodePath = destinationPath;
			return destinationPath;
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public CommitData detail(String id) {

		try {
			System.out.println("Trying to get info about " + id);
			SVNLogEntry entry = ((List<SVNLogEntry>) repository.log(
					null, null, Long.parseLong(id),
					Long.parseLong(id), true, true)).get(0);
			System.out.println("Done trying to get info about " + id);
			CommitData commitData = generateCommitData(id, entry);
			getDiffInfo(id, entry, commitData);

			return commitData;
		} catch (SVNException e) {
			throw new SCMException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void getDiffInfo(String id, SVNLogEntry entry, CommitData commitData)
			throws SVNException {
		for (SVNLogEntryPath change : (Collection<SVNLogEntryPath>) entry.getChangedPaths().values()) {
			SVNNodeKind nodeKind = repository.checkPath(change.getPath(), entry.getRevision());

			if (!nodeKind.toString().equals("dir")) {
				String diff = diffParser.parse(diffOn(change, id));
				commitData.addDiff(new DiffData(change.getPath(), diff, modificationOn(change), artifactTypeOn(diff)));
			}
		}
	}

	private CommitData generateCommitData(String id, SVNLogEntry entry) {
		CommitData commitData = new CommitData();
		commitData.setCommitId(String.valueOf(entry.getRevision()));
		commitData.setAuthor(entry.getAuthor());
		commitData.setEmail("N/A");
		commitData.setMessage(entry.getMessage());
		commitData.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(entry.getDate()));
		commitData.setPriorCommit(String.valueOf(Long.parseLong(id) - 1));
		return commitData;
	}

	private ArtifactKind artifactTypeOn(String diff) {
		return diff == null ? ArtifactKind.BINARY : ArtifactKind.CODE;
	}

	private String diffOn(SVNLogEntryPath change, String id) {
		return doDiff(repoPath, change.getPath(), Long.parseLong(id) - 1, Long.parseLong(id));
	}

	private String doDiff(final String url, final String filePath,
			final long revision1, final long revision2) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		SVNDiffClient diffClient = new SVNDiffClient(repository.getAuthenticationManager(), null);

		try {
			diffClient.doDiff(SVNURL.parseURIEncoded(url + filePath),
					SVNRevision.create(revision1),
					SVNURL.parseURIEncoded(url + filePath),
					SVNRevision.create(revision2),
					org.tmatesoft.svn.core.SVNDepth.UNKNOWN, true, result);
			repository.closeSession();
		} catch (SVNException ex) {
			repository.closeSession();
		}

		return result.toString();
	}

	private ModificationKind modificationOn(SVNLogEntryPath change) {
		switch (change.getType()) {
		case SVNLogEntryPath.TYPE_ADDED:
			return ModificationKind.NEW;
		case SVNLogEntryPath.TYPE_DELETED:
			return ModificationKind.DELETED;
		}

		return ModificationKind.DEFAULT;
	}

	public String sourceOf(String hash, String fileName) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			repository.getFile(fileName, Long.parseLong(hash), null, baos);
			return baos.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getSourceCodePath() {
		return sourceCodePath;
	}

	public String blame(String id, String file, int line) {
		try {
			SVNLogClient logClient = SVNClientManager.newInstance(null, repository.getAuthenticationManager()).getLogClient();
			
			SVNBlameHandler blamer = new SVNBlameHandler(line-1);
			logClient.doAnnotate(SVNURL.parseURIDecoded(repoPath + file), SVNRevision.UNDEFINED, SVNRevision.create(0), SVNRevision.create(Long.parseLong(id)), blamer);
			
			return blamer.getRevision();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void setup() {
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();
		FSRepositoryFactory.setup();
	}

}

import br.com.caelum.revolution.domain.ModificationKind;
import br.com.caelum.revolution.scm.DiffData;
		List<DiffData> diffs = new GitDiffParser().parse(log);
		List<DiffData> diffs = new GitDiffParser().parse(log);
		List<DiffData> diffs = new GitDiffParser().parse(log);
		assertEquals(ModificationKind.DEFAULT, diffs.get(0).getModificationKind());
		List<DiffData> diffs = new GitDiffParser().parse(log);
		assertEquals(ModificationKind.DELETED, diffs.get(0).getModificationKind());
		List<DiffData> diffs = new GitDiffParser().parse(log);
		assertEquals(ModificationKind.NEW, diffs.get(0).getModificationKind());
		List<DiffData> diffs = new GitDiffParser().parse(log);
		assertEquals(ModificationKind.NEW, diffs.get(0).getModificationKind());
	
	@Test
	public void shouldIgnoreBlankDiffs() {
		List<DiffData> diffs = new GitDiffParser().parse("");
		
		assertEquals(0, diffs.size());
	}
	
	@Test
	public void shouldTreatNewFilesWithNoDiff() {
		String log = "diff --git a/src/main/webapp/WEB-INF/jsp/comercial/turma/atualiza.invalid.jsp "+
					"b/src/main/webapp/WEB-INF/jsp/comercial/turma/atuaa.invalid.jsp\r\n"+
					"new file mode 100644" +
					"index 0000000..e69de29";
		
		List<DiffData> diffs = new GitDiffParser().parse(log);

		assertEquals(ModificationKind.NEW, diffs.get(0).getModificationKind());
		assertEquals("", diffs.get(0).getDiff());

		
	}
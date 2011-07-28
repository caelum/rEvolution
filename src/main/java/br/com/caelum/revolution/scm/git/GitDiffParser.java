import java.util.HashMap;
import java.util.Map;
import br.com.caelum.revolution.domain.ArtifactKind;
import br.com.caelum.revolution.domain.ModificationKind;
import br.com.caelum.revolution.scm.DiffData;
	private static Map<ModificationKind, String> map;
	
	static {
		map = new HashMap<ModificationKind, String>();
		map.put(ModificationKind.NEW, "new file mode");
		map.put(ModificationKind.DELETED, "deleted file mode");
		map.put(ModificationKind.DEFAULT, "nothing");
	}
	
	public List<DiffData> parse(String diff) {
		List<DiffData> allDiffs = new ArrayList<DiffData>();
			ModificationKind status = findStatusIn(lines);
			
			allDiffs.add(new DiffData(name, content, status, content.isEmpty() ?  ArtifactKind.BINARY : ArtifactKind.CODE));
	private int findTheLineWhereDiffStarts(List<String> lines) {
		int lineDiffsStarts = findTheLineWhereDiffStarts(lines);
		if(lineDiffsStarts == lines.size()) return "";
		
	private ModificationKind findStatusIn(List<String> lines) {
		int diffStarts = findTheLineWhereDiffStarts(lines);
		int modeLine = diffStarts == lines.size() ? 1 : diffStarts - 2;
		for(ModificationKind st : EnumSet.allOf(ModificationKind.class)) {
			if(lines.get(modeLine).startsWith(map.get(st))) return st;
		return ModificationKind.DEFAULT;
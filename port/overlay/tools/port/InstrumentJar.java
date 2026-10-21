import org.microemu.android.asm.AndroidProducer;

import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.*;

/**
 * Prepara o .jar do jogo para ser compilado junto com o app (flavor "midlet").
 *
 * Aplica nas classes a MESMA instrumentação ASM que o J2ME Loader usa quando
 * converte um .jar no app (dexlib: AndroidProducer) e copia os recursos
 * (imagens, .mid, .ui, ...) sem alteração. O MANIFEST.MF original é descartado
 * (o do app fica em app/src/midlet/resources/MIDLET-META-INF/MANIFEST.MF).
 *
 * Uso: java InstrumentJar <entrada.jar> <saida.jar>
 */
public class InstrumentJar {

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("uso: InstrumentJar <entrada.jar> <saida.jar>");
			System.exit(2);
		}
		File in = new File(args[0]);
		File out = new File(args[1]);
		File parent = out.getAbsoluteFile().getParentFile();
		if (!parent.isDirectory() && !parent.mkdirs()) {
			throw new IOException("Nao foi possivel criar " + parent);
		}
		int classes = 0, failed = 0, resources = 0;
		Set<String> seen = new HashSet<>();
		try (ZipFile zf = new ZipFile(in);
			 ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out)))) {
			Enumeration<? extends ZipEntry> en = zf.entries();
			while (en.hasMoreElements()) {
				ZipEntry e = en.nextElement();
				String name = e.getName();
				if (e.isDirectory() || name.startsWith("META-INF/") || !seen.add(name)) {
					continue;
				}
				byte[] data = readAll(zf.getInputStream(e));
				if (name.endsWith(".class")) {
					try {
						data = AndroidProducer.instrument(data, name);
						classes++;
					} catch (Throwable t) {
						failed++;
						System.err.println("AVISO: nao instrumentou " + name + ": " + t);
					}
				} else {
					resources++;
				}
				ZipEntry ne = new ZipEntry(name);
				ne.setTime(0L); // build reproduzivel
				zos.putNextEntry(ne);
				zos.write(data);
				zos.closeEntry();
			}
		}
		System.out.println("InstrumentJar: " + classes + " classes instrumentadas, "
				+ failed + " falhas, " + resources + " recursos -> " + out);
		if (failed > 0) {
			System.exit(1);
		}
	}

	private static byte[] readAll(InputStream is) throws IOException {
		try (InputStream in = is) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(Math.max(32, in.available()));
			byte[] buf = new byte[16384];
			int n;
			while ((n = in.read(buf)) > 0) {
				bos.write(buf, 0, n);
			}
			return bos.toByteArray();
		}
	}
}

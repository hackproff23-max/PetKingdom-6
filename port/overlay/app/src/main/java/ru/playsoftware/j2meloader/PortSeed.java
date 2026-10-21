package ru.playsoftware.j2meloader;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.playsoftware.j2meloader.config.Config;

/**
 * Usado apenas no APK "port" (flavor midlet, FULL_EMULATOR == false).
 *
 * Na primeira execucao copia para as pastas internas do app a configuracao que veio
 * junto no APK (assets/port/):
 *   - config.json              -> files/configs/midlet/config.json
 *   - VirtualKeyboardLayout    -> files/configs/midlet/VirtualKeyboardLayout
 *   - shaders/*                -> files/shaders/
 *
 * Assim o jogo ja abre direto, sem passar pela tela de configuracoes do J2ME Loader,
 * com o teclado virtual (cores, contorno, formato, posicao) e o shader que foram
 * exportados do app original.
 *
 * O config.json e o layout so sao copiados se ainda nao existirem: o que o usuario
 * mudar depois (ex.: mover botoes) nao e sobrescrito. Os shaders sao sempre
 * recopiados (sao pequenos e nao sao editados pelo usuario).
 */
public final class PortSeed {
	private static final String TAG = "PortSeed";
	private static final String ASSET_DIR = "port";

	private PortSeed() {
	}

	/** @param appPath mesmo caminho usado pelo MicroActivity (.../files/converted/midlet) */
	public static void seed(Context context, String appPath) {
		if (BuildConfig.FULL_EMULATOR) {
			return;
		}
		try {
			File appDir = new File(appPath);
			File converted = appDir.getParentFile();
			String workDir = converted == null ? null : converted.getParent();
			if (workDir == null) {
				return;
			}
			AssetManager assets = context.getAssets();

			File configDir = new File(workDir + Config.MIDLET_CONFIGS_DIR + appDir.getName());
			File config = new File(configDir, "config.json");
			if (!config.exists()) {
				if (!configDir.isDirectory() && !configDir.mkdirs()) {
					Log.e(TAG, "Nao foi possivel criar " + configDir);
					return;
				}
				copyAsset(assets, ASSET_DIR + "/config.json", config);
				File layout = new File(configDir, "VirtualKeyboardLayout");
				if (!layout.exists()) {
					copyAsset(assets, ASSET_DIR + "/VirtualKeyboardLayout", layout);
				}
			}

			File shadersDir = new File(workDir + Config.SHADERS_DIR);
			if (shadersDir.isDirectory() || shadersDir.mkdirs()) {
				String[] names = assets.list(ASSET_DIR + "/shaders");
				if (names != null) {
					for (String name : names) {
						copyAsset(assets, ASSET_DIR + "/shaders/" + name, new File(shadersDir, name));
					}
				}
			}
		} catch (Exception e) {
			// Nunca derruba o jogo por causa da semeadura: no pior caso abre a tela de config.
			Log.e(TAG, "seed failed", e);
		}
	}

	private static void copyAsset(AssetManager assets, String asset, File dst) throws IOException {
		try (InputStream in = assets.open(asset); OutputStream out = new FileOutputStream(dst)) {
			byte[] buf = new byte[8192];
			int n;
			while ((n = in.read(buf)) > 0) {
				out.write(buf, 0, n);
			}
		}
	}
}

import browserSyncLib from "browser-sync";
import fs from "fs";
import path from "path";
import { bundle as lightningBundle } from "lightningcss";
import resolve from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
import { rollup } from "rollup";

const browserSync = browserSyncLib.create();

const SOURCE_ROOT = "app";

const JS_ENTRY_LIST = [`./${SOURCE_ROOT}/index.js`];
const CSS_ENTRY_LIST = [`./${SOURCE_ROOT}/index.css`];
const OUTPUT_DIR = "./public";
const EXCLUDED_FOLDERS = [];

const WATCHED_ROOT = [
  `${OUTPUT_DIR}/**`,
  `./${SOURCE_ROOT}/**/*.html`,
  `./${SOURCE_ROOT}/**/*.js`,
  `./${SOURCE_ROOT}/**/*.css`,
];

// ------------------------
// Helpers
// ------------------------

function cleanBundle() {
  if (!fs.existsSync(OUTPUT_DIR)) {
    console.log(`Directory does not exist: ${OUTPUT_DIR}`);
    return;
  }

  const files = fs.readdirSync(OUTPUT_DIR);

  for (const file of files) {
    const filePath = path.join(OUTPUT_DIR, file);
    const stat = fs.statSync(filePath);

    if (stat.isFile()) fs.unlinkSync(filePath);
    else if (stat.isDirectory())
      fs.rmSync(filePath, { recursive: true, force: true });
  }

  console.log(`Cleaned all files from directory: ${OUTPUT_DIR}`);
}

async function buildJsBundle() {
  try {
    for (const input of JS_ENTRY_LIST) {
      const bundleObj = await rollup({
        input,
        plugins: [resolve(), commonjs()],
      });
      await bundleObj.write({ dir: OUTPUT_DIR, format: "umd" });
    }
    console.log(`Bundled JS successfully to ${OUTPUT_DIR}`);
  } catch (err) {
    console.error("Error during JS bundling:", err);
  }
}

async function buildCssBundle() {
  // Ensure output directory exists
  await fs.promises.mkdir(OUTPUT_DIR, { recursive: true });

  for (const entry of CSS_ENTRY_LIST) {
    try {
      // Verify the source file exists
      await fs.promises.access(entry, fs.constants.R_OK);

      // Bundle the CSS
      const { code } = await lightningBundle({
        filename: entry,
        minify: false,
      });

      // Write to output
      const outputPath = path.join(OUTPUT_DIR, path.basename(entry));
      await fs.promises.writeFile(outputPath, code);

      console.log(`Bundled CSS: ${entry} -> ${outputPath}`);
    } catch (err) {
      console.error(`Error bundling CSS file ${entry}:`, err.message);
    }
  }
}

async function copyFile(source, destination) {
  await fs.promises.mkdir(path.dirname(destination), { recursive: true });
  await fs.promises.copyFile(source, destination);
  console.log(`Copied ${source} -> ${destination}`);
}

// ------------------------
// Initial Build
// ------------------------
cleanBundle();
await buildCssBundle();
await buildJsBundle();

// ------------------------
// BrowserSync
// ------------------------
browserSync.init({
  files: WATCHED_ROOT,
  injectChanges: false,
  cors: true,
  open: false,
});

// ------------------------
// Watchers
// ------------------------

// JS
browserSync.watch(["./app/**/*.js"], async (event, file) => {
  if (event === "change") await buildJsBundle();
});

// CSS
browserSync.watch(["./app/**/*.css"], async (event, file) => {
  if (event === "change") await buildCssBundle();
});

// HTML
browserSync.watch(["./app/**/*.html"], async (event, file) => {
  const shouldExclude = EXCLUDED_FOLDERS.some((folder) =>
    file.includes(path.join(path.sep, folder, path.sep)),
  );

  if (!shouldExclude && (event === "change" || event === "add")) {
    const relativePath = path.relative(
      path.join(process.cwd(), SOURCE_ROOT),
      file,
    );
    const destination = path.join(process.cwd(), OUTPUT_DIR, relativePath);

    try {
      await copyFile(file, destination);
    } catch (err) {
      console.error(`Failed to copy HTML file ${file}:`, err.message);
    }
  }
});

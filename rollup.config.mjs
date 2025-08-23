import resolve from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
import terser from "@rollup/plugin-terser";
import { rollupPluginHTML as html } from "@web/rollup-plugin-html";
import { copy } from "@web/rollup-plugin-copy";
import { bundle } from "lightningcss";

const plugins = [
  html({
    minify: true,
    minifyJS: true, // handles inline <script> in HTML
    transformAsset: (_content, filePath) => {
      if (filePath.endsWith(".css")) {
        let { code } = bundle({
          filename: filePath,
          minify: true,
        });
        return new TextDecoder("utf-8").decode(code);
      }
    },
  }),
  resolve(),
  commonjs(),
  terser(), // run after HTML + bundling
  copy({ patterns: "./*.{txt}", exclude: "node_modules" }),
];

export default [
  {
    input: "./app/index.html",
    output: {
      dir: "public",
      entryFileNames: "[name].[hash].js",
    },
    plugins: plugins,
  },
];

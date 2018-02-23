/*
 * Copyright (C) 2018 Kristjan Hendrik Küngas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const convertPathsToAliases = require("convert-tsconfig-paths-to-webpack-aliases").default;
const tsconfig = require("./tsconfig.json");
const aliases = convertPathsToAliases(tsconfig);
const src = path.resolve(__dirname, './common/ui');

module.exports = {
  cache: true,
  context: src,
  entry: path.join(src, 'index.tsx'),
  output: {
    publicPath: '/',
    path: path.resolve(__dirname, './gateway/main/resources/static/dist')
  },
  resolve: {
    alias: aliases,
    extensions: ['.ts', '.tsx', '.js', '.jsx', '.json'],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: path.join(src, 'index.html'),
    }),
  ],
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        loader: 'awesome-typescript-loader'
      }
    ]
  }
};

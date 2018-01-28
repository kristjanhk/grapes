const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const src = path.resolve(__dirname, './src');

module.exports = {
  cache: true,
  context: src,
  entry: path.join(src, 'index.tsx'),
  output: {
    publicPath: '/',
    path: path.resolve(__dirname, './main/resources/static/dist')
  },
  resolve: {
    alias: {
      css: path.join(src, 'css'),
      img: path.join(src, 'images'),
      font: path.join(src, 'fonts'),
      music: path.join(src, 'music'),
      components: path.join(src, 'components'),
    },
    extensions: ['.js', '.jsx', '.json', '.ts', '.tsx'],
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

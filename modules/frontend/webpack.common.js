const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const paths = {
  nodeModules: path.resolve(__dirname, './node_modules'),
  src: path.resolve(__dirname, './src'),
  dist: path.resolve(__dirname, './main/resources/static/dist'),
  css: path.resolve(__dirname, './assets/static/css'),
  img: path.resolve(__dirname, './assets/static/img'),
  font: path.resolve(__dirname, './assets/static/font')
};

module.exports = {
  cache: true,
  context: paths.src,
  entry: paths.src + '/index.tsx',
  output: {
    publicPath: '/',
    path: paths.dist
  },
  resolve: {
    alias: {
      css: paths.css,
      img: paths.img,
      font: paths.font
    },
    extensions: ['.js', '.jsx', '.json', '.ts', '.tsx']
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: paths.src + '/index.html'
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

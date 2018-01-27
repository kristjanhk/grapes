const Merge = require('webpack-merge');
const CommonConfig = require('./webpack.common');

module.exports = new Merge(CommonConfig, {
  output: {
    filename: '[name].js'
  },
  devtool: 'source-map',
  devServer: {
    hot: true,
    hotOnly: true,
    historyApiFallback: true,
    overlay: true,
    contentBase: 'src',
    publicPath: '/'
  },
  module: {
    rules: [
      {
        enforce: 'pre',
        test: /\.js$/,
        loader: 'source-map-loader'
      }
    ]
  },
  plugins: [

  ]
});

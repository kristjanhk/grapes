const Merge = require('webpack-merge');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const CommonConfig = require('./webpack.common');

module.exports = new Merge(CommonConfig, {
  output: {
    filename: '[name].[hash].js',
    publicPath: '/static/dist/'
  },
  plugins: [
    //new CleanWebpackPlugin(['./main/resources/static/dist']),
  ]
});

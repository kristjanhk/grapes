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

import {Howl} from "howler";
import * as React from "react";
import * as Eventbus from "vertx3-eventbus-client";

export interface ITestProps {
  compiler: string;
  framework: string;
}

export class Test extends React.Component<ITestProps, {}> {
  private howler: Howl;

  constructor(props: ITestProps) {
    super(props);
    this.howler = new Howl({
      format: ["mp3"],
      html5: true,
      src: "https://kyngas.eu/radio/stream",
    });
    this.play = this.play.bind(this);
    const eb = new Eventbus("url");
  }

  public play() {
    this.howler.play();
  }

  public render() {
    return (
        <div>
          <h1>Hello from {this.props.compiler} and {this.props.framework}!</h1>
          <a onClick={this.play}>Start radio</a>
        </div>
    );
  }
}

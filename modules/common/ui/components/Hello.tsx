import {Howl} from "howler";
import {SpotifyLocalService} from "music/spotify-ts/spotify_local_service-proxy";
import {Test} from "music/Test";
import * as React from "react";
import * as Eventbus from "vertx3-eventbus-client";

export interface IHelloProps {
  compiler: string;
  framework: string;
}

export class Hello extends React.Component<IHelloProps, {}> {
  private howler: Howl;

  constructor(props: IHelloProps) {
    super(props);
    this.howler = new Howl({
      format: ["mp3"],
      html5: true,
      src: "https://kyngas.eu/radio/stream",
    });
    this.play = this.play.bind(this);
    const eb = new Eventbus("url");
    const spotify = new SpotifyLocalService(eb, "url");
    spotify.close();
  }

  public play() {
    this.howler.play();
  }

  public render() {
    return (
        <div>
          <h1>Hello from {this.props.compiler} and {this.props.framework}!</h1>
          <a onClick={this.play}>Start radio</a>
          <Test compiler="TypeScript" framework="React"/>
        </div>
    );
  }
}

import {Howl} from "howler";
import {SpotifyLocalService} from "music/gen/spotify-ts/spotify_local_service-proxy";
import * as React from "react";
import * as Eventbus from "vertx3-eventbus-client";

export interface IHelloProps {
  compiler: string;
  framework: string;
}

export interface IState {
  ready: boolean;
}

export class Hello extends React.Component<IHelloProps, IState> {
  private howler: Howl;
  private spotify: SpotifyLocalService;

  constructor(props: IHelloProps) {
    super(props);
    this.state = {
      ready: false,
    };
    this.howler = new Howl({
      format: ["mp3"],
      html5: true,
      src: "https://kyngas.eu/radio/stream",
    });
    //this.howler.play();
    this.playUri = this.playUri.bind(this);
    const eb = new Eventbus("https://kyngas.eu/eventbus");
    eb.onopen = () => {
      this.spotify = new SpotifyLocalService(eb, "music.spotify.local");
      this.setState({
        ready: true,
      });
    };
  }

  public playUri(event: any) {
    if (event.key === "Enter") {
      console.log("Value: " + event.target.value);
      this.spotify.playTrack(event.target.value, (err, res) => {
        console.log("Response: err -> " + JSON.stringify(err) + ", res -> " + JSON.stringify(res));
      });
    }
  }

  public play() {
    this.howler.play();
  }

  public render() {
    return (
        <div>
          <h1>Hello from {this.props.compiler} and {this.props.framework}!</h1>
          {this.state.ready ? <input onKeyPress={this.playUri}/> : null}
          <a onClick={this.play}>Start radio</a>
        </div>
    );
  }
}

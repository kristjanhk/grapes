import {Howl} from "howler";
import * as React from "react";

export interface IHelloProps {
  compiler: string;
  framework: string;
}

export class Hello extends React.Component<IHelloProps, {}> {
  constructor(props: IHelloProps) {
    super(props);
    const howler = new Howl({
      format: ["mp3"],
      html5: true,
      src: "https://kyngas.eu/radio/stream",
    });
    howler.play();
  }
  public render() {
    return <h1>Hello from {this.props.compiler} and {this.props.framework}!</h1>;
  }
}

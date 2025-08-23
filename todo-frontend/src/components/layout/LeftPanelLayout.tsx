import { Col, Image } from "antd";
import loginImage from '../../assets/login.svg';
import styled from "styled-components";

const ImageCotainer = styled(Image)`
  @media (max-width: 768px) {
    width: 80%;
    height: 80%;
  }
 @media (max-width: 560px) {
    visibility: hidden;
    disabled: true;
    width: 10px;
}`

export default function LeftPanelLayout() {
    return (
        <Col span={12} style={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
          <ImageCotainer
            width={750}
            height={500}
            preview={false}
            src={loginImage}
          />
        </Col>
    )
}
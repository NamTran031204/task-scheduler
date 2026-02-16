import axios from "axios";
import { URL } from "../constants/routeURL";
import { LOGIN_API } from "../constants/apiConstants";

// const token = localStorage.getItem("token");

// export interface RegisterInput {
//   username: string;
//   password: string;
//   email: string;
//   fullName: string;
// }

export const loginUser = async (userData: any) => {
  try {
    const response = await axios.post(`${URL}` + `${LOGIN_API}`, userData, {
      headers: {
        "Content-Type": "application/json",
        // Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error login user:", error);
    throw error;
  }
};
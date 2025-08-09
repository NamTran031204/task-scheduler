import axios from "axios";
import { URL } from "../constants/routeURL";
import { REGISTER_API } from "../constants/apiConstants";

// const token = localStorage.getItem("token");

export interface RegisterInput {
  username: string;
  password: string;
  email: string;
  fullName: string;
}

export const registerUser = async (userData: any) => {
  try {
    const response = await axios.post(`${URL}` + `${REGISTER_API}`, userData, {
      headers: {
        "Content-Type": "application/json",
        // Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error registering user:", error);
    throw error;
  }
};
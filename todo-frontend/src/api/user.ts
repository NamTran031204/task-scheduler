import axios from "axios";
import { URL } from "../constants/routeURL";
import {
  UPDATE_AVATAR_API,
  GET_USER_API,
  GET_ALL_USERS_API,
  UPDATE_USER_API,
  DELETE_USER_API,
} from "../constants/apiConstants";


export interface UserResponse {
  id: number;
  username: string;
  email: string;
  fullName?: string | null;
  avatarUrl?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UserListResponse {
  userResponses: UserResponse[];
  totalPage: number;
}



// 1.4 Get User by ID
export const getUserById = async (userId: number): Promise<UserResponse> => {
  const { data } = await axios.get(`${URL}${GET_USER_API}/${userId}`);
  return data;
};

// 1.5 Get All Users (Paginated)
export const getAllUsers = async (
  record: number,
  page: number
): Promise<UserListResponse> => {
  const { data } = await axios.get(`${URL}${GET_ALL_USERS_API}`, {
    params: { record, page },
  });
  return data;
};

// 1.3 Update User Avatar
export const updateUserAvatar = async (
  userId: number,
  file: File
): Promise<string> => {
  const formData = new FormData();
  formData.append("file", file);

  const { data } = await axios.post(
    `${URL}${UPDATE_AVATAR_API}/${userId}`,
    formData,
    {
      headers: { "Content-Type": "multipart/form-data" },
    }
  );
  return data;
};

// 1.6 Update User (info and/or avatar)
export const updateUser = async (
  userId: number,
  payload: {
    username?: string;
    fullname?: string;
    avatar?: File;
  }
): Promise<string> => {
  const formData = new FormData();
  if (payload.username) formData.append("username", payload.username);
  if (payload.fullname) formData.append("fullname", payload.fullname);
  if (payload.avatar) formData.append("avatar", payload.avatar);

  const { data } = await axios.put(
    `${URL}${UPDATE_USER_API}/${userId}`,
    formData,
    {
      headers: { "Content-Type": "multipart/form-data" },
    }
  );
  return data;
};

// 1.7 Delete User
export const deleteUser = async (userId: number): Promise<string> => {
  const { data } = await axios.delete(`${URL}${DELETE_USER_API}/${userId}`);
  return data;
};

import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  userId:'',
  fullName: ''
};

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    setUserData: (state, action) => {
      const userData = action.payload;
      localStorage.setItem('user', JSON.stringify(userData));
      return { ...state, ...userData };
    },
    clearUserData: () => {
      localStorage.removeItem('user');
      return initialState;
    }
  }
});

export const { setUserData, clearUserData } = userSlice.actions;
export default userSlice.reducer;
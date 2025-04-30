import { createStore } from 'redux';

// Initial state
const initialState = {
  user: {
    name: '',
  }
};

// Reducer
function userReducer(state = initialState, action) {
  switch (action.type) {
    case 'SET_USER_DATA':
      return {
        ...state,
        user: {
          name: action.payload.name,
        }
      };
    default:
      return state;
  }
}

// Create store
const store = createStore(userReducer);

export default store;
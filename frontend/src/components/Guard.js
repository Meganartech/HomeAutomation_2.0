import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

export default function Guard({ children }) {
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const handlePopState = () => {
            const token = localStorage.getItem('token');
            if (token) {
                const confirmLogout = window.confirm("Going back will log you out. Do you want to proceed?");
                if (confirmLogout) {
                    localStorage.removeItem('token');
                    navigate('/');
                } else {
                    navigate(location.pathname);
                }
            }
        };

        window.addEventListener('popstate', handlePopState);
        return () => window.removeEventListener('popstate', handlePopState);
    }, [navigate, location]);

    return children;
}
import { useLocation } from 'react-router-dom';

export default function Indicator() {
    const location = useLocation();

    const pathSegments = location.pathname.split('/').filter(Boolean);

    const capitalize = (str) => str.charAt(0).toUpperCase() + str.slice(1);

    return (
        <div className="bg-white p-4 rounded shadow-sm mb-3">
            <h4 className="m-0">
                {capitalize(pathSegments[0])}
                {pathSegments[1] && <> &nbsp;›&nbsp; {capitalize(pathSegments[1])}</>}
            </h4>
        </div>
    );
};
import { Link, useLocation } from "react-router-dom";

export default function Sidebar() {
    const location = useLocation();

    const menuItems = [
        { path: "/user/profile", label: "Profile" },
        { path: "/user/profile/update", label: "Profile Update" },
        { path: "/user/room", label: "Add Room" },
        { path: "/user/device", label: "Add Device" },
        { path: "/user/scan", label: "Scan Device" }
    ];

    const activeItem = menuItems.find(item => item.path === location.pathname)?.label || "Menu";

    return (
        <>
            {/*large screens */}
            <nav className="col-md-3 col-xl-2 d-none d-md-block shadow-lg bg-D4C9BE sidebar p-3">
                <ul className="nav flex-column">
                    {menuItems.map((item, index) => (
                        <li className="nav-item mb-3" key={index}>
                            <Link className={`nav-link sidebar-hover text-123458 ${location.pathname === item.path ? "active-123458" : ""}`} to={item.path}>{item.label}</Link>
                        </li>
                    ))}
                </ul>
            </nav>

            {/* Small Screens */}
            <div className="d-md-none p-3">
                <div className="dropdown">
                    <button className="btn btn-123458 dropdown-toggle shadow-lg" type="button" data-bs-toggle="dropdown" aria-expanded="false">{activeItem}</button>
                    <ul className="dropdown-menu bg-D4C9BE p-2">
                        {menuItems.map((item, index) => (
                            <li key={index}>
                                <Link className={`dropdown-item sidebar-hover text-123458 rounded mb-2 ${location.pathname === item.path ? "active-123458" : ""}`} to={item.path}>{item.label}</Link>
                            </li>
                        ))}
                    </ul>
                </div>
            </div>
        </>
    );
}
